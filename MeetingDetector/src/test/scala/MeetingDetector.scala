package meetingdetector

import org.scalatest.FunSuite
import java.util.Date

class MeetingDetectorSpec extends FunSuite {
    val l1 = Location(100, 100, 1)
    val e1 = LocationEvent(new Date(1473626604000L), l1, "u1")
    val l2 = Location(105, 105, 1)
    val e2 = LocationEvent(new Date(1473626608000L), l2, "u2")

    test("isMeetingOnSameFloor returns true if location events on same floor") {
        val detector = MeetingDetector(5, 5)
        assert(detector.isMeetingOnSameFloor(e1 -> e2))
    }

    test("isMeetingOnSameFloor returns false if location events on different floors") {
        val detector = MeetingDetector(5, 5)
        val e3 = e2.copy(location = l2.copy(floor = 2))
        assert(!detector.isMeetingOnSameFloor(e1 -> e3))
    }

    test("isMeetingWithinMaxDistanceRange returns true if location events are less then max distance away") {
        val detector = MeetingDetector(15, 5)
        assert(detector.isMeetingWithinMaxDistanceRange(e1 -> e2))
        assert(detector.isMeetingWithinMaxDistanceRange(e2 -> e1))
    }

    test("isMeetingWithinMaxDistanceRange returns false if location events are more then max distance away") {
        val detector = MeetingDetector(5, 5)
        assert(!detector.isMeetingWithinMaxDistanceRange(e1 -> e2))
        assert(!detector.isMeetingWithinMaxDistanceRange(e2 -> e1))
    }

    test("isMeetingWithincMaxTimeRange returns true if location events occured within max time") {
        val detector = MeetingDetector(5, 5)
        assert(detector.isMeetingWithinMaxTimeRange(e1 -> e2))
        assert(detector.isMeetingWithinMaxTimeRange(e2 -> e1))
    }

    test("isMeetingWithingMaxTimeRange returns false if location events occured more then max time appart") {
        val detector = MeetingDetector(5, 2)
        assert(!detector.isMeetingWithinMaxTimeRange(e1 -> e2))
        assert(!detector.isMeetingWithinMaxTimeRange(e2 -> e1))
    }

    test("isMeetingClose returns true when all clauses true") {
        val detector = MeetingDetector(15, 5)
        assert(detector.isMeetingClose(e1 -> e2))
        assert(detector.isMeetingClose(e2 -> e1))
    }

    test("findMeetings returns meeting events when users meet") {

        val allLocationEvents = List(
            e1.copy(user_id = "u3"),
            e1.copy(user_id = "u3"),
            e1.copy(location = l1.copy(floor = 2)),
            e1.copy(location = l1.copy(floor = 2)),
            e2.copy(user_id = "u4"),
            e2.copy(user_id = "u4"),
            e2,
            e1.copy(location = l2.copy(floor = 2)),
            e1
        )

        val detector = MeetingDetector(15, 5)
        val meetingEvents = detector.findMeetings("u1", "u2", allLocationEvents.iterator)

        assert(meetingEvents.hasNext)
        val meetingEvent = meetingEvents.next()
        assert(meetingEvent._1 == e1)
        assert(meetingEvent._2 == e2)
    }

    test("findMeetings returns no meeting events when users did not meet") {

        val allLocationEvents = List(
            e1.copy(user_id = "u3"),
            e1.copy(user_id = "u3"),
            e1.copy(location = l1.copy(floor = 2)),
            e1.copy(location = l1.copy(floor = 2)),
            e2.copy(user_id = "u4"),
            e2.copy(user_id = "u4"),
            e2,
            e1.copy(location = l2.copy(floor = 2))
        )

        val detector = MeetingDetector(15, 5)
        val meetingEvents = detector.findMeetings("u1", "u2", allLocationEvents.iterator)

        assert(!meetingEvents.hasNext)
    }
}