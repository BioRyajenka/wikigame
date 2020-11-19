package network

import TimeProvider
import state.Millis
import state.action.UserAction
import mu.KotlinLogging

/**
 * This class holds event buffer. The buffer is needed to smooth out the
 * UPD packet reordering effect. Each event also supplied with receiving time.
 *
 * There are three ways to deal with UDP packets:
 * 1) apply each packet with some delay. drop latecomers
 * 2) apply each packet with some delay. apply latecomers on top
 * 3) apply each packet with some delay. reorder
 *    not-very-latecomers (and reapply events to older snapshots of state). drop the rest
 *
 * I will start with first and then go to the second and third if there will be any necessity
 *
 * Regarding time window, I will use approach which will guarantee that events will be
 * executed in range [PERIOD;PERIOD*2] after their receivement(4-8ms for 4ms PERIOD).
 * PERIOD is the period in which some special thread applies some events from queue.
 * This is close to the best guarantee we want to expect: we want to apply each packet after 5ms
 * after receiving it (because practical approximate reordering lag is 5ms)
 *
 * @author Igor Sushencev
 * @since 28.01.19
 * Copyright (c) Huawei Technologies Co., Ltd. 2015-2019. All rights reserved.
 */
private val logger = KotlinLogging.logger {}

class EventBuffer(private val relaxationPeriod: Millis, private val eventConsumer: (UserAction) -> Unit) {
    @Volatile
    private var buffer = BufferWithOffset(0)

    /**
     * This function is not suited for running from multiple threads
     * So it should be run from epoll-like network system
     */
    fun scheduleEvent(event: UserAction) {
        // TODO: validation will be on the upper level
        val bufferToUse = buffer
        bufferToUse[event.actionId] = event
        if (bufferToUse !== buffer) {
            // buffer changed. event could either be added or not added. just repeat
            println("buffer changed for ${event.actionId}")
            scheduleEvent(event)
        }
    }

    /**
     * Called every relaxationPeriod ms
     */
    fun relax() {
        val relaxationStartTime = TimeProvider.currentTime

        // we use internal buffer snapshot because internal buffer object may change
        val rightmostApplicableEvent = (0 until buffer.size).indexOfLast { i ->
            val it = buffer[i]
            it != null && relaxationStartTime - it.aroseAtTime >= relaxationPeriod
        }

        if (rightmostApplicableEvent == -1) return

        val oldBuffer = buffer
        buffer = BufferWithOffset(
            oldBuffer.bufferStartId + rightmostApplicableEvent + 1,
            null
        )

        // build nested buffer
        val nestedBuffer = BufferWithOffset(oldBuffer.bufferStartId, null)
        val doubleNestedBuffer = oldBuffer.nestedReadOnlyBuffer

        if (doubleNestedBuffer != null) {
            val shiftToNested = oldBuffer.bufferStartId - doubleNestedBuffer.bufferStartId
            // only interesting events
            (buffer.bufferStartId - doubleNestedBuffer.bufferStartId until doubleNestedBuffer.size).forEach { i ->
                val el = doubleNestedBuffer[i]
                if (el != null) {
                    nestedBuffer[i - shiftToNested] = el
                }
            }
        }
        (0 until oldBuffer.size).forEach { i ->
            val el = oldBuffer[i]
            if (el != null) {
                nestedBuffer[i] = el
            }
        }
        buffer.nestedReadOnlyBuffer = nestedBuffer

        // relax
        // rightmostApplicableEvent may be not 100% recent, but it is ok
        (0..rightmostApplicableEvent).forEach { i ->
            val event = oldBuffer[i]
            if (event == null) {
                logger.warn("Event ${oldBuffer.bufferStartId + i} is lost")
                println("event ${oldBuffer.bufferStartId + i} проебан")
                return@forEach
            }
            eventConsumer(event)
        }
    }
}

private const val INITIAL_BUFFER_SIZE = 16 // on each relaxation iteration

private class BufferWithOffset(
    val bufferStartId: Int,
    var nestedReadOnlyBuffer: BufferWithOffset? = null
) {
    @Volatile
    private var buffer: Array<UserAction?> = arrayOfNulls<UserAction?>(INITIAL_BUFFER_SIZE)

    val size: Int
        get() = buffer.size

    // guaranteed to be called only from one thread simultaneously
    operator fun get(pos: Int): UserAction? {
        val valueFromNestedReadOnlyBuffer = if (nestedReadOnlyBuffer != null) {
            val shiftToNested = bufferStartId - nestedReadOnlyBuffer!!.bufferStartId
            val nestedBufferPos = pos + shiftToNested
            if (nestedBufferPos >= nestedReadOnlyBuffer!!.size) null else nestedReadOnlyBuffer!!.buffer[nestedBufferPos]
        } else null
        return valueFromNestedReadOnlyBuffer ?: buffer[pos]
    }

    operator fun set(rawPos: Int, event: UserAction) {
        val pos = rawPos - bufferStartId

        if (pos < 0) {
            logger.warn("Dropping latecomer event: rawPos is $rawPos and bufferStartId is $bufferStartId")
            return
        }

        extendBufferToAtLeast(pos + 1)
        if (buffer[pos] != null) {
            // TODO: add equals to the echo.UDP
            if (buffer[pos]!! != event) {
                logger.warn("Received events differ: ${buffer[pos]} and $event")
            }
            return
        }
//        println("${event.actionId} is written to ${hashCode()} $pos")
        buffer[pos] = event
    }

    private fun extendBufferToAtLeast(pos: Int) {
        var newSize = buffer.size
        while (newSize <= pos) newSize *= 2
        buffer = buffer.copyOf(newSize)
    }
}
