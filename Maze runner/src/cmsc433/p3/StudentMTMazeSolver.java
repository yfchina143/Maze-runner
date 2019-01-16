package cmsc433.p3;

import java.util.*;
import java.util.concurrent.*;

/**
 * This file needs to hold your solver to be tested.
 * You can alter the class to extend any class that extends MazeSolver.
 * It must have a constructor that takes in a Maze.
 * It must have a solve() method that returns the datatype List<Direction>
 * which will either be a reference to a list of steps to take or will
 * be null if the maze cannot be solved.
 */
public class StudentMTMazeSolver extends SkippingMazeSolver {
    private ConcurrentHashMap<Position, Direction> visited;
    private final int numThread;
    private ExecutorService fixedThreadPool;
    List<Callable<List<Direction>>> returnList;
    List<Direction> rl2;

    public StudentMTMazeSolver(Maze maze) {
        super(maze);
        numThread = Runtime.getRuntime().availableProcessors();
        fixedThreadPool = Executors.newFixedThreadPool(numThread);
        returnList = new ArrayList<Callable<List<Direction>>>();
        rl2 = new ArrayList<Direction>();
        visited = new ConcurrentHashMap<Position, Direction>();
        //System.out.println("this is num:"+numThread);
    }

    public List<Direction> solve() {
        // TODO: Implement your code here
        List<Direction> solutions = null;
        try {
            Choice firstStart = firstChoice(maze.getStart());
            while (!firstStart.choices.isEmpty()) {
                //  rl2.add(firstStart.choices.peek());
                returnList.add(new childSolver(follow(firstStart.at, firstStart.choices.peek()), firstStart.choices.pop()));
            }
        } catch (SolutionFound e) {

        } finally {
            try {
                for (int i = 0; i < returnList.size(); i++) {

                    Future<List<Direction>> temp = fixedThreadPool.submit(returnList.get(i));
                    //    rl2.get(i);
                    solutions = temp.get();

                    if (solutions != null) {
                        break;
                    }
                }
            } catch (Exception e1) {

                e1.printStackTrace();
            } finally {
                fixedThreadPool.shutdown();
            }

            return solutions;
        }
    }

    private class childSolver implements Callable<List<Direction>> {

        Choice currentChoice;
        Direction from;


        public childSolver(Choice startPoint, Direction from) {
            currentChoice = startPoint;
            this.from = from;
        }

        @Override
        public List<Direction> call() {
            //solve();


            LinkedList<Choice> choiceStack = new LinkedList<Choice>();
            Choice ch;

            try {
                int i;
                choiceStack.push(firstChoice(maze.getStart()));
                while (!choiceStack.isEmpty()) {
                    int p;
                    ch = choiceStack.peek();
                    if (ch.isDeadend()) {
                        // backtrack.
                        choiceStack.pop();
                        if (!choiceStack.isEmpty()) choiceStack.peek().choices.pop();
                        continue;
                    }
                    choiceStack.push(follow(ch.at, ch.choices.peek()));
                }
                // No solution found.
                return null;
            } catch (SolutionFound e) {
                int j;
                Iterator<Choice> iter = choiceStack.iterator();
                LinkedList<Direction> solutionPath = new LinkedList<Direction>();
                while (iter.hasNext()) {
                    ch = iter.next();
                    solutionPath.push(ch.choices.peek());
                }
                if (maze.display != null) maze.display.updateDisplay();
                solutionPath.push(from);
                return pathToFullPath(solutionPath);
            }
        }
    }
}
