package meetingdetector

import java.util.Date

import scala.io.Source
import scala.util.{Failure, Success, Try}

object LocationEventReader {

    val format = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")

    /**
      * Parse a CSV string into a possible [[meetingdetector.LocationEvent]].
      *
      * @param eventCSV CSV serialized event
      * @return a possible LocationEvent
      */
    private def parseEventCSV(eventCSV: String): Try[LocationEvent] = {

        val eventParts = eventCSV.split(",").map(_.trim)
        if (eventParts.length != 5) {
            return Failure(
                createValidationError("Unexpected number of values", eventCSV)
            )
        }

        //TODO - handle validation failures, to return Failure
        val locationX: Double = eventParts(1).toDouble
        val locationY: Double = eventParts(2).toDouble
        val floor: Int = eventParts(3).toInt
        val location = Location(locationX, locationY, floor)

        val timestamp: Date = format.parse(eventParts(0))
        val user_id: UserID = eventParts(4)

        Success(
            LocationEvent(timestamp, location, user_id)
        )
    }

    private def createValidationError(errorMsg: String, line: String) = {
        val msg = "%s in \"%s\"".format(errorMsg, line)
        new IllegalArgumentException(msg)
    }

    /**
      * Reads an input file of location events in CSV format and provides an iterator
      * of location events. In case of a validation failures on a single lines the error
      * message is printed to stderr and the event is ignored.
      *
      * @param inputFilePath input file of location events
      * @return iterator of location events
      */
    def readLocationEvents(inputFilePath: String): Iterator[LocationEvent] = {
        val lines = Source.fromFile(inputFilePath).getLines()
        var possibleEvents: Iterator[Try[LocationEvent]] = lines.drop(1).map(parseEventCSV)

        //filter only valid events, print warning to stderr on invalid events,
        possibleEvents = possibleEvents.filter {
            case Failure(error) => {
                println(error); false
            };
            case _ => true
        }

        possibleEvents.map(_.get)
    }
}
