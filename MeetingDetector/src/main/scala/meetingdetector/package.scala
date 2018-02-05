import java.util.Date

package object meetingdetector {

    type UserID = String

    case class Location(var x: Double, var y: Double, var floor: Int)

    case class LocationEvent(timestamp: Date, location: Location, user_id:UserID)

    type MeetingEvent = (LocationEvent, LocationEvent)
}
