package meetingdetector

/**
  * Implements logic to determine if a meeting occurred between two users, bases on a stream of
  * location events. Two users are considered to have met, is all the following clauses are true:
  * - Both user's location is on the same floor
  * - The distance between 2 users is less then meetingDistanceMetersThreshold
  * - The time between 2 users events is less then meetingTimeSecondsThreshold
  *
  * @param meetingDistanceMetersThreshold max distance threshold
  * @param meetingTimeSecondsThreshold    max time threshold
  */
case class MeetingDetector(meetingDistanceMetersThreshold: Double, meetingTimeSecondsThreshold: Double) {

    private val closeMeetingClauses: List[MeetingEvent => Boolean] = List(
        isMeetingOnSameFloor,
        isMeetingWithinMaxTimeRange,
        isMeetingWithinMaxDistanceRange
    )

    /**
      * Determine when meetings occur between two users, and return an iterator of each
      * meeting event. Handles an incoming stream of unfiltered location events for any user.
      * Assumes that the location events coming in are in sorted timestamp order.
      *
      * @param firstUserID       userID of the first user
      * @param secondUserID      userID of the second user
      * @param allLocationEvents iterator of all location events
      * @return iterator of all meeting events between the two users
      */
    def findMeetings(firstUserID: UserID,
                     secondUserID: UserID,
                     allLocationEvents: Iterator[LocationEvent]): Iterator[MeetingEvent] = {

        val userIDS = Set(firstUserID, secondUserID)
        val usersLocationEvents = allLocationEvents.filter(e => userIDS.contains(e.user_id))

        val allMeetingEvents = findAllMeetingEvents(firstUserID, secondUserID, usersLocationEvents)

        allMeetingEvents.filter(isMeetingClose)
    }

    def isMeetingOnSameFloor(meetingEvent: MeetingEvent): Boolean = {
        meetingEvent._1.location.floor == meetingEvent._2.location.floor
    }

    def isMeetingWithinMaxTimeRange(meetingEvent: MeetingEvent): Boolean = {
        val timeDiffSec = (meetingEvent._1.timestamp.getTime() - meetingEvent._2.timestamp.getTime) / 1000.0
        math.abs(timeDiffSec) <= meetingTimeSecondsThreshold
    }

    def isMeetingWithinMaxDistanceRange(meetingEvent: MeetingEvent): Boolean = {
        val fstLoc = meetingEvent._1.location
        val sndLoc = meetingEvent._2.location
        val distanceDiff = math.sqrt(
            (fstLoc.x - sndLoc.x) * (fstLoc.x - sndLoc.x) +
                (fstLoc.y - sndLoc.y) * (fstLoc.y - sndLoc.y)
        )
        distanceDiff <= meetingDistanceMetersThreshold
    }


    def isMeetingClose(meetingEvent: MeetingEvent): Boolean = {
        closeMeetingClauses.forall(_ (meetingEvent))
    }

    private def findAllMeetingEvents(firstUserID: UserID,
                                     secondUserID: UserID,
                                     usersLocationEvents: Iterator[LocationEvent]): Iterator[MeetingEvent] = {

        var lastLocationEvents: Map[UserID, LocationEvent] = Map()

        val possibleMeetingEvents: Iterator[(Option[LocationEvent], Option[LocationEvent])] = for {
            locationEvent: LocationEvent <- usersLocationEvents
            //empty assigment to cause a side-effect to lastLocationEvents.
            // TODO - determine if there is a better scala-idiomatic way to accomplish this
            _ = {
                lastLocationEvents = lastLocationEvents + (locationEvent.user_id -> locationEvent)
                Unit
            }
        } yield (lastLocationEvents.get(firstUserID), lastLocationEvents.get(secondUserID))

        possibleMeetingEvents.collect {
            case (Some(fstLocationEvent), Some(sndLocationEvent)) => (fstLocationEvent -> sndLocationEvent)
        }
    }
}
