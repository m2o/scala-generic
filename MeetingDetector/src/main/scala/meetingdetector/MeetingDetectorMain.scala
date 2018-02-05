package meetingdetector

import scopt.OptionParser

/** Command line application for the detection of a meeting between users, based on locality data.
  *
  * The application parses an input stream of location events for various users, continously checking
  * if two users have met. The application will print to stdout all the pairs of location events that
  * constitute a meeting, if these have occured. See [[meetingdetector.MeetingDetectorMain]] for details
  * about the clauses that have to be meet for a meeting to have occurred. Thresholds for time and space
  * proximity are configurable via command line arguments (meetingDistanceMetersThreshold and
  * meetingTimeSecondsThreshold), although defaults are provided.
  *
  * Possible improvements to the application:
  * - Implement edge-triggering, by detecting changes in state and emitting only events when users
  * locations "come into" a meeting and "come out" of a meeting.
  * - Double check and optimize if required so the application is a proper filter, and scales to
  * support arbitrary sized input streams, doesn't load all events in RAM.
  * - Expand unit test coverage
  * - Handle more input validation failures
  * - Determine if there is a better, scala idiomatic way to handle printing validation failures to stderr
  * - Determine is there is a better, scala idiomatic way to organize packages/namespaces
  */
object MeetingDetectorMain {

    private case class Config(inputFile: String = "",
                              meetingDistanceMetersThreshold: Double = 2.0,
                              meetingTimeSecondsThreshold: Double = 5.0,
                              firstUserID: UserID = "",
                              secondUserID: UserID = "")

    private def parseCmdArgs(args: Array[String]): Config = {
        val parser = new OptionParser[Config]("meetingdetector") {
            head("meetingdetector", "0.1")

            opt[String]('f', "inputFile").required().valueName("<file>").
                action((x, c) => c.copy(inputFile = x)).
                text("Input file containing location events in CSV format")

            opt[UserID]('a', "firstUserID").required().action((x, c) =>
                c.copy(firstUserID = x)).text("User ID of the first user")

            opt[UserID]('b', "secondUserID").required().action((x, c) =>
                c.copy(secondUserID = x)).text("User ID of the second user")

            opt[Double]('d', "meetingDistanceMetersThreshold").action((x, c) =>
                c.copy(meetingDistanceMetersThreshold = x))
                .text("Minimum distance (in m) between two user's locations to consider it a meeting")

            opt[Double]('t', "meetingTimeSecondsThreshold").action((x, c) =>
                c.copy(meetingTimeSecondsThreshold = x))
                .text("Minimum time (in s) between two user's location events to consider it a meeting")

            help("help").text("Prints this usage text")

            note("Command line application for the detection of a meeting between users, based on locality data.\n")
        }

        parser.parse(args, Config()) match {
            case Some(config) => config
            case None => {
                sys.exit(2)
            }
        }
    }

    /**
      * Used a meeting detector to find and print to stdout meetings between two users.
      *
      * @param fstUserID      userID of the first user
      * @param sndUserID      userID of the second user
      * @param detector       a meeting detector
      * @param locationEvents an iterator of location events
      * @return boolean determing if at least one meeting occurred
      */
    private def printMeetings(fstUserID: UserID,
                              sndUserID: UserID,
                              detector: MeetingDetector,
                              locationEvents: Iterator[LocationEvent]): Boolean = {

        println(s"Attempting to find meeting between users %s and %s ".format(fstUserID, sndUserID))
        val meetingEvents: Iterator[MeetingEvent] = detector.findMeetings(fstUserID, sndUserID, locationEvents)

        if (meetingEvents.hasNext) {
            println("The following meetings occurred:")
            meetingEvents.foreach(println)
            true
        } else {
            println("No meetings occurred")
            false
        }
    }

    def main(args: Array[String]): Unit = {
        val config: Config = parseCmdArgs(args)
        val locationEvents = LocationEventReader.readLocationEvents(config.inputFile)
        val detector = MeetingDetector(config.meetingDistanceMetersThreshold, config.meetingTimeSecondsThreshold)
        sys.exit(if (printMeetings(config.firstUserID, config.secondUserID, detector, locationEvents)) 0 else 1)
    }
}
