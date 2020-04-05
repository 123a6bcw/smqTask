import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.BufferedOutputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.lang.Integer.max
import java.lang.Integer.min
import java.util.InputMismatchException
import java.util.Random
import java.util.Scanner

internal class MainKtTest {

    private fun initialiseConsoleInput(input: String, printStream: PrintStream) {
        System.setIn(ByteArrayInputStream(input.toByteArray()))
        System.setOut(printStream)
    }

    private fun runTest(input: String): String {
        val byteStream = ByteArrayOutputStream()
        PrintStream(BufferedOutputStream(byteStream)).use { printer ->
            initialiseConsoleInput(input, printer)
            main()
        }

        return byteStream.toString()
    }

    private fun runTest(input: String, expected: String) {
        val result = runTest(input)
        assertEquals(expected, result)
    }

    private fun runTestWithNaiveSolution(random: Random) {
        val numberOfTrains = 10
        val modulo = 20

        val inputBuilder = StringBuilder()
        inputBuilder.append(numberOfTrains).append('\n')
        for (i in 0 until numberOfTrains) {
            inputBuilder.append(' ').append(i)
            for (t in 0 until 3) {
                inputBuilder.append(' ').append(random.nextInt(modulo) + 1)
            }
            inputBuilder.append('\n')
        }

        val input = inputBuilder.toString()
        val resultExpected = naiveSolution(input)
        val resultActual = runTest(input)

        assertEquals(resultExpected.toString(), resultActual.lines().first(), "fail on test:\n $input")
    }

    @Test
    fun testOneTrain() {
        val input = """
            1
            1 0 2 100
            
        """.trimIndent()

        val expected = """
            100
            [1]
            
        """.trimIndent()

        runTest(input, expected)
    }

    @Test
    fun testSameArrivalAndDepartingTrainsShouldGiveTrainWithMaximumReward() {
        val input = """
            5
            3 0 5 30
            1 0 5 10
            2 0 5 20
            5 0 5 50
            4 0 5 40
            
        """.trimIndent()

        val expected = """
            50
            [5]
            
        """.trimIndent()

        runTest(input, expected)
    }

    @Test
    fun testTrainWithArrivalJustAfterPreviousTrainsDeparting() {
        val input = """
            6
            3 0 5 10
            1 0 5 10
            2 0 5 20
            4 0 5 60
            5 0 5 50
            6 5 5 11
        """.trimIndent()

        val expected = """
            71
            [4, 6]
            
        """.trimIndent()

        runTest(input, expected)
    }

    @Test
    fun testOneGoodTrainPathFloodedWithWorseTrains() {
        val input = """
            17
            10 0  5 2
            11 0  6 2
            8  0  5 1
            22 5  7 1
            20 5  7 2
            21 5  9 3
            23 6  8 3
            30 13 4 3
            31 13 4 2
            32 15 9 3
            41 18 2 3
            40 18 2 4
            50 20 3 5
            51 20 3 4
            52 19 3 5
            53 19 3 6
            54 21 3 4
            
        """.trimIndent()

        val expected = """
            ${2 + 2 + 3 + 4 + 5}
            [10, 20, 30, 40, 50]
            
        """.trimIndent()

        runTest(input, expected)
    }

    @Test
    fun testOneGoodTrainThatOutweighsAllOvers() {
        val input = """
            18
            1  12 5  10
            2  13 4  15
            3  0  2  16
            4  5  3  12
            5  6  4  10
            6  4  1  9
            7  8  2  12
            18 0  99 1000
            8  0  7  11
            9  6  5  13
            10 1  4  10
            11 7  3  11
            12 18 2  16
            13 12 1  19
            14 6  2  18
            15 7  2  20
            16 0  1  20
            17 13 4  20
            
        """.trimIndent()

        val expected = """
            1000
            [18]
            
        """.trimIndent()

        runTest(input, expected)
    }

    @Test
    fun testSeveralGoodPathsWithAlmostSamePerformance() {
        val input = """
            20

            11 0 4 1
            12 4 5 3
            13 9 3 2
            14 12 4 3
            15 16 10 5
            
            21 0 5   2
            22 5 5   2
            23 10 5  5
            24 15 5  4
            25 20 10 7
            
            31 0 7 2
            32 7 3 1
            33 10 4 4
            34 14 2 1
            35 16 10 1
            
            41 0  8  1
            42 8  4  1
            43 12 1  3
            44 13 14 4
            45 27 1  5
            
        """.trimIndent()

        val expected = """
            20
            [21, 22, 23, 24, 25]
            
        """.trimIndent()

        runTest(input, expected)
    }

    @Test
    fun testAllTrainsWithSameRewardShouldGiveTheLongestPath() {
        val input = """
            22
            9 0 3 1
            8 0 7 1
            6 7 5 1
            5 6 8 1
            10 0 1 1
            15 3 5 1
            11 1 2 1
            17 6 9 1
            18 6 8 1
            19 6 4 1
            20 6 3 1
            12 3 4 1
            13 7 1 1
            21 7 2 1
            22 6 3 1
            23 5 4 1 
            14 8 5 1
            24 7 6 1
            25 7 7 1
            26 5 10 1
            27 4 13 1
            28 4 4 1
        """.trimIndent()

        val expected = """
            5
            [10, 11, 12, 13, 14]
            
        """.trimIndent()

        runTest(input, expected)
    }

    @Test
    fun testRandomWithNaiveSolution() {
        val seed = 13L
        val numberOfTests = 10

        val random = Random(seed)
        for (i in 0 until numberOfTests) {
            runTestWithNaiveSolution(random)
        }
    }

    private fun naiveSolution(input: String): Int {
        class Train(val id: Int, val arriveTime: Int, val unloadTime: Int, val reward: Int) {
            val departureTime = arriveTime + unloadTime
        }

        val trains = mutableListOf<Train>()

        Scanner(input).use { reader ->
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
                return -1
            } catch (e: InputMismatchException) {
                return -1
            }
        }

        var bestReward = 0

        fun trainChosenByMask(mask: Int, index: Int): Boolean {
            return mask and (1 shl index) > 0
        }

        for (mask in 0 until (1 shl trains.size)) {
            var reward = 0
            for (i in 0 until trains.size) {
                if (trainChosenByMask(mask, i)) {
                    reward += trains[i].reward
                }
            }

            if (reward < bestReward) {
                continue
            }

            var maskIsCorrect = true

            for (i in 0 until trains.size) {
                for (j in 0 until trains.size) {
                    if (i != j && trainChosenByMask(mask, i) && trainChosenByMask(mask, j)) {
                        if (max(trains[i].arriveTime, trains[j].arriveTime) < min(
                                trains[i].departureTime,
                                trains[j].departureTime
                            )
                        ) {
                            //intersection is not empty
                            maskIsCorrect = false
                        }
                    }
                }
            }

            if (maskIsCorrect) {
                bestReward = reward
            }
        }

        return bestReward
    }
}