package com.example.game

import kotlin.math.abs
import kotlin.random.Random

data class PuzzleTile(
    val id: Int, // Original piece index (0 until N*N - 1) or BLANK_ID
    val originalRow: Int,
    val originalCol: Int
)

data class PuzzleState(
    val gridSize: Int,
    val tiles: List<Int>, // List of length N*N, storing tile IDs. BLANK_ID represents empty space.
    val blankIndex: Int,
    val moveCount: Int,
    val isSolved: Boolean
)

object SlidingPuzzleEngine {
    const val BLANK_ID = -1

    /**
     * Creates a guaranteed solvable shuffled puzzle state by performing valid random slides
     * starting from the solved state.
     */
    fun createShuffledPuzzle(gridSize: Int): PuzzleState {
        val totalTiles = gridSize * gridSize
        // Solved state: 0, 1, 2, ..., totalTiles-2, BLANK_ID
        val board = (0 until totalTiles - 1).toMutableList().apply {
            add(BLANK_ID)
        }
        var blankPos = totalTiles - 1

        // Number of random shuffle moves based on grid size
        val shuffleMoves = when (gridSize) {
            3 -> 60
            4 -> 120
            else -> 180
        }

        var lastSwappedPos = -1
        repeat(shuffleMoves) {
            val validNeighbors = getAdjacentIndices(blankPos, gridSize).filter { it != lastSwappedPos }
            val chosenNeighbor = if (validNeighbors.isNotEmpty()) {
                validNeighbors[Random.nextInt(validNeighbors.size)]
            } else {
                getAdjacentIndices(blankPos, gridSize).random()
            }

            // Swap blank with chosenNeighbor
            board[blankPos] = board[chosenNeighbor]
            board[chosenNeighbor] = BLANK_ID
            lastSwappedPos = blankPos
            blankPos = chosenNeighbor
        }

        // If somehow already solved after shuffle, do 2 valid moves
        if (isSolved(board, gridSize)) {
            val neighbors = getAdjacentIndices(blankPos, gridSize)
            val n1 = neighbors.first()
            board[blankPos] = board[n1]
            board[n1] = BLANK_ID
            blankPos = n1
        }

        return PuzzleState(
            gridSize = gridSize,
            tiles = board.toList(),
            blankIndex = blankPos,
            moveCount = 0,
            isSolved = false
        )
    }

    /**
     * Returns true if clicking clickedIndex is valid and slides the tile into the blank slot.
     */
    fun moveTile(state: PuzzleState, clickedIndex: Int): PuzzleState? {
        if (state.isSolved) return null
        if (clickedIndex < 0 || clickedIndex >= state.tiles.size) return null
        if (state.tiles[clickedIndex] == BLANK_ID) return null

        val gridSize = state.gridSize
        val blankIndex = state.blankIndex

        val r1 = clickedIndex / gridSize
        val c1 = clickedIndex % gridSize
        val r2 = blankIndex / gridSize
        val c2 = blankIndex % gridSize

        val isAdjacent = (abs(r1 - r2) == 1 && c1 == c2) || (abs(c1 - c2) == 1 && r1 == r2)
        if (!isAdjacent) return null

        val newTiles = state.tiles.toMutableList()
        newTiles[blankIndex] = newTiles[clickedIndex]
        newTiles[clickedIndex] = BLANK_ID

        val solved = isSolved(newTiles, gridSize)

        return state.copy(
            tiles = newTiles,
            blankIndex = clickedIndex,
            moveCount = state.moveCount + 1,
            isSolved = solved
        )
    }

    fun isSolved(tiles: List<Int>, gridSize: Int): Boolean {
        val totalTiles = gridSize * gridSize
        for (i in 0 until totalTiles - 1) {
            if (tiles[i] != i) return false
        }
        return tiles[totalTiles - 1] == BLANK_ID
    }

    fun getAdjacentIndices(index: Int, gridSize: Int): List<Int> {
        val neighbors = mutableListOf<Int>()
        val row = index / gridSize
        val col = index % gridSize

        if (row > 0) neighbors.add(index - gridSize) // Up
        if (row < gridSize - 1) neighbors.add(index + gridSize) // Down
        if (col > 0) neighbors.add(index - 1) // Left
        if (col < gridSize - 1) neighbors.add(index + 1) // Right

        return neighbors
    }
}
