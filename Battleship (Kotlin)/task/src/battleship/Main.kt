package battleship

import kotlin.math.abs

data class Ship(val type: String, val size: Int)

const val LETTERS = "ABCDEFGHIJ"

val ships = listOf(
    Ship("Aircraft Carrier", 5),
    Ship("Battleship", 4),
    Ship("Submarine", 3),
    Ship("Cruiser", 3),
    Ship("Destroyer", 2)
)

val gameBoard = LETTERS.mapIndexed { _, char -> char to mutableListOf("~", "~", "~", "~", "~", "~", "~", "~", "~", "~") }.toMap()
val gameBoard2 = LETTERS.mapIndexed { _, char -> char to mutableListOf("~", "~", "~", "~", "~", "~", "~", "~", "~", "~") }.toMap()

val allocatedShips = ships.associate { ship -> ship.type to mutableListOf<String>() }
val allocatedShips2 = ships.associate { ship -> ship.type to mutableListOf<String>() }
fun allocateShips(allocatedShips: Map<String, MutableList<String>>, shipName: String, coordinate: String) {
    allocatedShips[shipName]?.add(coordinate)
}

val afloatShips = ships.map { it.type }.toMutableList()
val afloatShips2 = ships.map { it.type }.toMutableList()
fun didAfloatShipsSink(allocatedShips: Map<String, MutableList<String>>, hitShots: MutableList<String>, afloatShips: MutableList<String>): Boolean {
    val copiedList = afloatShips.toMutableList()
    //println("allocatedShips=$allocatedShips")
    for (shipName in copiedList) {
        if (allocatedShips[shipName]?.let { hitShots.containsAll(it) } == true) {
            afloatShips.remove(shipName)
            return true
        }
    }
    return false
}

val hitShots = mutableListOf<String>()
val hitShots2 = mutableListOf<String>()
fun registerHitShots(hitShots: MutableList<String>, coordinate: String) {
    hitShots.add(coordinate)
}

fun playerShipFilling(player: String, gameBoard: Map<Char, MutableList<String>>, ships: List<Ship>, allocatedShips: Map<String, MutableList<String>>) {
    println("$player, place your ships on the game field")
    printTable(gameBoard)
    for (ship in ships) {
        println("Enter the coordinates of the ${ship.type} (${ship.size} cells):")
        fillGameBoard(gameBoard, ship.type, ship.size, allocatedShips)
        printTable(gameBoard)
    }
    println("Press Enter and pass the move to another player")
    val wait = readln()
}

fun playerFillShots(player: String, myGameBoard: Map<Char, MutableList<String>>, enemyGameBoard: Map<Char, MutableList<String>>, hitShots: MutableList<String>, allocatedShips: Map<String, MutableList<String>>, afloatShips: MutableList<String>): Boolean {
    printFogOfWar(myGameBoard)
    println("---------------------")
    printTable(myGameBoard)
    println("$player, it's your turn:")
    fillShotCoord(enemyGameBoard, hitShots, allocatedShips, afloatShips)
    if (afloatShips.isEmpty()) return true
    println("Press Enter and pass the move to another player")
    val wait = readln()
    return false
}

fun main() {

    playerShipFilling("Player 1", gameBoard, ships, allocatedShips)
    playerShipFilling("Player 2", gameBoard2, ships, allocatedShips2)

    var player = "Player 1"
    while (true) {
        if (player == "Player 1") {
            val isGameOver = playerFillShots(player, gameBoard, gameBoard2, hitShots, allocatedShips2, afloatShips2)
            if (isGameOver) break
            player = "Player 2"
        } else {
            val isGameOver = playerFillShots(player, gameBoard2, gameBoard, hitShots2, allocatedShips, afloatShips)
            if (isGameOver) break
            player = "Player 1"
        }

    }

}

fun printTable(gameField: Map<Char, MutableList<String>>) {
    println("  1 2 3 4 5 6 7 8 9 10")
    for ((key, value) in gameField) {
        print("$key ")
        for (cell in value) print("$cell ")
        println()
    }
}

fun printFogOfWar(gameField: Map<Char, MutableList<String>>) {
    println("  1 2 3 4 5 6 7 8 9 10")
    for ((key, value) in gameField) {
        print("$key ")
        for (cell in value) {
            if (cell != "~") print("~ ")
            else print("$cell ")
        }
        println()
    }
}


fun fillGameBoard(gameBoard: Map<Char, MutableList<String>>, shipType: String, shipSize: Int, allocatedShips: Map<String, MutableList<String>>) {
    // println("allocatedShips=$allocatedShips")
    val coordinates = cleanUserInput()
    println("$coordinates, ${coordinates.first}, ${coordinates.second}")
    if (validateInputSize(coordinates.first, coordinates.second, shipSize)) {
        val startRow = coordinates.first[0]
        val startCol = coordinates.first.substring(1).toInt()
        val endRow = coordinates.second[0]
        val endCol = coordinates.second.substring(1).toInt()
        println("startRow=$startRow, startCol=$startCol, endRow=$endRow, endCol=$endCol,")

        if (startRow == endRow) {
            val startNum = if (startCol < endCol) startCol else endCol
            val endNum = if (startCol < endCol) endCol else startCol
            println("startNum=$startNum, endNum=$endNum")
            if (validateHorizontalFogCells(gameBoard, startRow, startNum, endNum)) {
                for (i in startNum..endNum) {
                    gameBoard[startRow]?.set(i - 1, "O")
                    allocateShips(allocatedShips, shipType, "$startRow$i")
                }
            } else {
                println("Error! Wrong ship location! Try again:")
                fillGameBoard(gameBoard, shipType, shipSize, allocatedShips)
            }
        } else {
            if (validateVerticalFogCells(gameBoard, startRow, startCol, shipSize)) {
                val smallRow = if (startRow < endRow) startRow else endRow
                for (i in 0 until shipSize) {
                    val newChar = smallRow + i
                    gameBoard[newChar]?.set(startCol - 1, "O")
                    allocateShips(allocatedShips, shipType, "$newChar$startCol")
                }
            } else {
                println("Error! Wrong ship location! Try again:")
                fillGameBoard(gameBoard, shipType, shipSize, allocatedShips)
            }
        }
    } else {
        println("Error! Wrong length of the $shipType! Try again:")
        fillGameBoard(gameBoard, shipType, shipSize, allocatedShips)
    }
}


fun cleanUserInput(): Pair<String, String> {
    val userInput = readln().trim()
    val coords = userInput.split(" ")
    return Pair(coords[0], coords[1])
}


fun validateInputSize(start: String, end: String, shipSize: Int): Boolean {
    println("validateInputSize $start, $end")
    if (validateCoordinates(start, end)) {
        val startRow = start.first()
        val startCol = start.substring(1).toInt()
        val endRow = end.first()
        val endCol = end.substring(1).toInt()
        println("shipSize=$shipSize, abs(endCol - startCol) + 1=${abs(endCol - startCol) + 1}, abs(endRow - startRow) + 1=${abs(endRow - startRow) + 1}")
        if (startRow != endRow && startCol != endCol) return false
        if (startRow == endRow) return abs(endCol - startCol) + 1 == shipSize
        if (startCol == endCol) return abs(endRow - startRow) + 1 == shipSize
    }
    return false
}

fun validateCoordinates(start: String, end: String): Boolean {
    println("validateCoordinates $start, $end")
    return validateSingleCoordinate(start) && validateSingleCoordinate(end)
}

fun validateSingleCoordinate(coordinate: String): Boolean {
    val pattern = Regex("^[A-J](10|[1-9])$")
    // println("coordinate.matches(pattern) = ${coordinate.matches(pattern)}")
    return coordinate.matches(pattern)
}

fun validateHorizontalFogCells(gameBoard: Map<Char, MutableList<String>>, startRow: Char, startNum: Int, endNum: Int): Boolean {
    for (i in startNum..endNum) {
        if (gameBoard[startRow]?.get(i-1) == "O") return false
    }
    return true
}

fun validateVerticalFogCells(gameBoard: Map<Char, MutableList<String>>, startRow: Char, startCol:Int, shipSize: Int): Boolean {
    for (i in 0 until shipSize) {
        val newChar = startRow + i
        if (gameBoard[newChar]?.get(startCol - 1) == "O") return false
    }
    return true
}

fun shotCoordInput(): String {
    val shotCoord = readln().trim()
    return shotCoord
}

fun fillShotCoord(gameBoard: Map<Char, MutableList<String>>, hitShots: MutableList<String>, allocatedShips: Map<String, MutableList<String>>, afloatShips: MutableList<String>) {
    val shotCoord = shotCoordInput()
    if (validateSingleCoordinate(shotCoord)) {
        hitOrMiss(gameBoard, shotCoord, hitShots, allocatedShips, afloatShips)
        //printTable(gameBoard)
    } else {
        println("Error! You entered the wrong coordinates! Try again:")
        fillShotCoord(gameBoard, hitShots, allocatedShips, afloatShips)
    }
}

fun hitOrMiss(gameBoard: Map<Char, MutableList<String>>, shotCoord: String, hitShots: MutableList<String>, allocatedShips: Map<String, MutableList<String>>, afloatShips: MutableList<String>) {
    // println("afloatShips=$afloatShips")
    // println("hitShots=$hitShots")
    // println("allocatedShips=$allocatedShips")
    val row = shotCoord.first()
    val col = shotCoord.substring(1).toInt()
    if (gameBoard[row]?.get(col-1) == "O" || gameBoard[row]?.get(col-1) == "X") {
        gameBoard[row]?.set(col-1, "X")
        registerHitShots(hitShots, shotCoord)
        //printFogOfWar(gameBoard)
        if (didAfloatShipsSink(allocatedShips, hitShots, afloatShips)) {
            if (afloatShips.isEmpty()) println("You sank the last ship. You won. Congratulations!")
            else println("You sank a ship! Specify a new target:")
        } else {
            println("You hit a ship! Try again:")
        }
    } else {
        gameBoard[row]?.set(col-1, "M")
        //printFogOfWar(gameBoard)
        println("You missed. Try again:")
    }
}