import java.util.InputMismatchException
import java.util.Scanner

private val errorMessage = """
    Wrong input format or error reading from the console. 

    Input should be as follows (without brackets):\n"
    
    <Number of trains>\n
    Then for each train:
    <Train number: Int> <Time of arrival: Int> <Time to unload the train: Int> <Reward for unloading the train: Int>
""".trimIndent()

private class Train(val id: Int, val arriveTime: Int, val unloadTime: Int, val reward: Int) {
    val departureTime = arriveTime + unloadTime
}

private class MaxRewardInfo(val maxReward: Int, val previousTrainNumber: Int, val takeThisTrain: Boolean)

fun main() {
    val trains = mutableListOf<Train>()

    Scanner(System.`in`).use { reader ->
        try {
            val numberOfTrains = reader.nextInt()
            for (i in 0 until numberOfTrains) {
                trains.add(
                    Train(
                        id = reader.nextInt(),
                        arriveTime = reader.nextInt(),
                        unloadTime = reader.nextInt(),
                        reward = reader.nextInt()
                    )
                )
            }
        } catch (e: NoSuchElementException) {
            println(errorMessage)
            return
        } catch (e: InputMismatchException) {
            println(errorMessage)
            return
        }
    }

    trains.sortWith(compareBy<Train> { it.departureTime }.thenBy { it.id })

    val maxRewardInfos = mutableListOf<MaxRewardInfo>()

    for ((index, train) in trains.withIndex()) {
        if (index == 0) {
            maxRewardInfos.add(MaxRewardInfo(train.reward, -1, true))
            continue
        }

        val lastAvailableTrain = findLastTrainDepartingBeforeThisTrainArrival(trains, index)

        val rewardNotTakingThisTrain = maxRewardInfos[index - 1].maxReward

        val rewardTakingThisTrain = train.reward +
            if (lastAvailableTrain >= 0) {
                maxRewardInfos[lastAvailableTrain].maxReward
            } else {
                0
            }

        maxRewardInfos.add(
            if (rewardNotTakingThisTrain > rewardTakingThisTrain) {
                MaxRewardInfo(rewardNotTakingThisTrain, index - 1, false)
            } else {
                MaxRewardInfo(rewardTakingThisTrain, lastAvailableTrain, true)
            }
        )
    }

    println(maxRewardInfos.last().maxReward)
    val trainIdsInAnswer = mutableListOf<Int>()
    var currentTrainNumber = trains.size - 1
    while (currentTrainNumber >= 0) {
        val maxRewardInfo = maxRewardInfos[currentTrainNumber]

        if (maxRewardInfo.takeThisTrain) {
            trainIdsInAnswer.add(trains[currentTrainNumber].id)
        }

        currentTrainNumber = maxRewardInfo.previousTrainNumber
    }

    println(trainIdsInAnswer.reversed())
}

private fun findLastTrainDepartingBeforeThisTrainArrival(trains: List<Train>, thisTrainIndex: Int): Int {
    var low = -1
    var high = thisTrainIndex

    while (low + 1 < high) {
        val mid = (low + high).ushr(1)

        if (trains[mid].departureTime <= trains[thisTrainIndex].arriveTime) {
            low = mid
        } else {
            high = mid
        }
    }

    return low
}