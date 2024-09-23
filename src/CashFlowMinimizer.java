import java.util.*;
import java.util.stream.Collectors;

class Bank {
    public String name;
    public int netAmount;
    public Set<String> types;

    public Bank() {
        this.types = new HashSet<>();
    }
}

public class CashFlowMinimizer {

    public static int getMinIndex(Bank[] listOfNetAmounts, int numBanks) {
        int min = Integer.MAX_VALUE, minIndex = -1;
        for (int i = 0; i < numBanks; i++) {
            if (listOfNetAmounts[i].netAmount == 0) continue;
            if (listOfNetAmounts[i].netAmount < min) {
                minIndex = i;
                min = listOfNetAmounts[i].netAmount;
            }
        }
        return minIndex;
    }

    public static int getSimpleMaxIndex(Bank[] listOfNetAmounts, int numBanks) {
        int max = Integer.MIN_VALUE, maxIndex = -1;
        for (int i = 0; i < numBanks; i++) {
            if (listOfNetAmounts[i].netAmount == 0) continue;
            if (listOfNetAmounts[i].netAmount > max) {
                maxIndex = i;
                max = listOfNetAmounts[i].netAmount;
            }
        }
        return maxIndex;
    }

    public static Pair<Integer, String> getMaxIndex(Bank[] listOfNetAmounts, int numBanks, int minIndex, Bank[] input, int maxNumTypes) {
        int max = Integer.MIN_VALUE;
        int maxIndex = -1;
        String matchingType = "";

        for (int i = 0; i < numBanks; i++) {
            if (listOfNetAmounts[i].netAmount == 0 || listOfNetAmounts[i].netAmount < 0) continue;

            List<String> intersection = listOfNetAmounts[minIndex].types.stream()
                    .filter(listOfNetAmounts[i].types::contains)
                    .collect(Collectors.toList());

            if (!intersection.isEmpty() && listOfNetAmounts[i].netAmount > max) {
                max = listOfNetAmounts[i].netAmount;
                maxIndex = i;
                matchingType = intersection.get(0);
            }
        }
        return new Pair<>(maxIndex, matchingType);
    }

    public static void printAns(List<List<Pair<Integer, String>>> ansGraph, int numBanks, Bank[] input) {
        System.out.println("\nThe transactions for minimum cash flow are as follows: \n");
        for (int i = 0; i < numBanks; i++) {
            for (int j = 0; j < numBanks; j++) {
                if (i == j) continue;

                if (ansGraph.get(i).get(j).getFirst() != 0 && ansGraph.get(j).get(i).getFirst() != 0) {
                    if (ansGraph.get(i).get(j).getFirst().equals(ansGraph.get(j).get(i).getFirst())) {
                        ansGraph.get(i).get(j).setFirst(0);
                        ansGraph.get(j).get(i).setFirst(0);
                    } else if (ansGraph.get(i).get(j).getFirst() > ansGraph.get(j).get(i).getFirst()) {
                        ansGraph.get(i).get(j).setFirst(ansGraph.get(i).get(j).getFirst() - ansGraph.get(j).get(i).getFirst());
                        ansGraph.get(j).get(i).setFirst(0);
                        System.out.println(input[i].name + " pays Rs " + ansGraph.get(i).get(j).getFirst() + " to " + input[j].name + " via " + ansGraph.get(i).get(j).getSecond());
                    } else {
                        ansGraph.get(j).get(i).setFirst(ansGraph.get(j).get(i).getFirst() - ansGraph.get(i).get(j).getFirst());
                        ansGraph.get(i).get(j).setFirst(0);
                        System.out.println(input[j].name + " pays Rs " + ansGraph.get(j).get(i).getFirst() + " to " + input[i].name + " via " + ansGraph.get(j).get(i).getSecond());
                    }
                } else if (ansGraph.get(i).get(j).getFirst() != 0) {
                    System.out.println(input[i].name + " pays Rs " + ansGraph.get(i).get(j).getFirst() + " to " + input[j].name + " via " + ansGraph.get(i).get(j).getSecond());
                } else if (ansGraph.get(j).get(i).getFirst() != 0) {
                    System.out.println(input[j].name + " pays Rs " + ansGraph.get(j).get(i).getFirst() + " to " + input[i].name + " via " + ansGraph.get(j).get(i).getSecond());
                }

                ansGraph.get(i).get(j).setFirst(0);
                ansGraph.get(j).get(i).setFirst(0);
            }
        }
        System.out.println("\n");
    }

    public static void minimizeCashFlow(int numBanks, Bank[] input, Map<String, Integer> indexOf, int numTransactions, List<List<Integer>> graph, int maxNumTypes) {
        Bank[] listOfNetAmounts = new Bank[numBanks];
        for (int i = 0; i < numBanks; i++) {
            listOfNetAmounts[i] = new Bank();
            listOfNetAmounts[i].name = input[i].name;
            listOfNetAmounts[i].types = input[i].types;

            int amount = 0;
            for (int j = 0; j < numBanks; j++) {
                amount += (graph.get(j).get(i) - graph.get(i).get(j));
            }

            listOfNetAmounts[i].netAmount = amount;
        }

        List<List<Pair<Integer, String>>> ansGraph = new ArrayList<>();
        for (int i = 0; i < numBanks; i++) {
            List<Pair<Integer, String>> row = new ArrayList<>();
            for (int j = 0; j < numBanks; j++) {
                row.add(new Pair<>(0, ""));
            }
            ansGraph.add(row);
        }

        int numZeroNetAmounts = 0;
        for (Bank b : listOfNetAmounts) {
            if (b.netAmount == 0) numZeroNetAmounts++;
        }

        while (numZeroNetAmounts != numBanks) {
            int minIndex = getMinIndex(listOfNetAmounts, numBanks);
            Pair<Integer, String> maxAns = getMaxIndex(listOfNetAmounts, numBanks, minIndex, input, maxNumTypes);
            int maxIndex = maxAns.getFirst();

            if (maxIndex == -1) {
                ansGraph.get(minIndex).get(0).setFirst(Math.abs(listOfNetAmounts[minIndex].netAmount));
                ansGraph.get(minIndex).get(0).setSecond(input[minIndex].types.iterator().next());

                int simpleMaxIndex = getSimpleMaxIndex(listOfNetAmounts, numBanks);
                ansGraph.get(0).get(simpleMaxIndex).setFirst(Math.abs(listOfNetAmounts[minIndex].netAmount));
                ansGraph.get(0).get(simpleMaxIndex).setSecond(input[simpleMaxIndex].types.iterator().next());

                listOfNetAmounts[simpleMaxIndex].netAmount += listOfNetAmounts[minIndex].netAmount;
                listOfNetAmounts[minIndex].netAmount = 0;

                if (listOfNetAmounts[minIndex].netAmount == 0) numZeroNetAmounts++;
                if (listOfNetAmounts[simpleMaxIndex].netAmount == 0) numZeroNetAmounts++;
            } else {
                int transactionAmount = Math.min(Math.abs(listOfNetAmounts[minIndex].netAmount), listOfNetAmounts[maxIndex].netAmount);

                ansGraph.get(minIndex).get(maxIndex).setFirst(transactionAmount);
                ansGraph.get(minIndex).get(maxIndex).setSecond(maxAns.getSecond());

                listOfNetAmounts[minIndex].netAmount += transactionAmount;
                listOfNetAmounts[maxIndex].netAmount -= transactionAmount;

                if (listOfNetAmounts[minIndex].netAmount == 0) numZeroNetAmounts++;
                if (listOfNetAmounts[maxIndex].netAmount == 0) numZeroNetAmounts++;
            }
        }

        printAns(ansGraph, numBanks, input);
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System
                .in);

        System.out.println("\n\t\t\t\t********************* Welcome to CASH FLOW MINIMIZER SYSTEM ***********************\n\n\n");
        System.out.println("This system minimizes the number of transactions among multiple banks in different corners of the world that use different modes of payment.");
        System.out.println("There is one world bank (with all payment modes) to act as an intermediary between banks that have no common mode of payment. \n\n");

        System.out.println("Enter the number of banks participating in the transa0ctions.");
        int numBanks = sc.nextInt();

        Bank[] input = new Bank[numBanks];
        Map<String, Integer> indexOf = new HashMap<>(); // stores index of a bank

        System.out.println("Enter the details of the banks and transactions as stated:");
        System.out.println("Bank name, number of payment modes it has and the payment modes.");
        System.out.println("Bank name and payment modes should not contain spaces");

        int maxNumTypes = 0;
        for (int i = 0; i < numBanks; i++) {
            input[i] = new Bank();
            if (i == 0) {
                System.out.print("World Bank: ");
            } else {
                System.out.print("Bank " + i + ": ");
            }
            input[i].name = sc.next();
            indexOf.put(input[i].name, i);

            int numTypes = sc.nextInt();
            if (i == 0) maxNumTypes = numTypes;

            for (int j = 0; j < numTypes; j++) {
                String type = sc.next();
                input[i].types.add(type);
            }
        }

        System.out.println("Enter number of transactions.");
        int numTransactions = sc.nextInt();

        List<List<Integer>> graph = new ArrayList<>();
        for (int i = 0; i < numBanks; i++) {
            List<Integer> row = new ArrayList<>(Collections.nCopies(numBanks, 0));
            graph.add(row);
        }

        System.out.println("Enter the details of each transaction as stated:");
        System.out.println("Debtor Bank, creditor Bank and amount");
        for (int i = 0; i < numTransactions; i++) {
            System.out.print(i + "th transaction: ");
            String s1 = sc.next();
            String s2 = sc.next();
            int amount = sc.nextInt();

            graph.get(indexOf.get(s1)).set(indexOf.get(s2), amount);
        }

        // Settle the transactions
        minimizeCashFlow(numBanks, input, indexOf, numTransactions, graph, maxNumTypes);
    }

    // Helper class for Pair (in place of C++ pair)
    static class Pair<U, V> {
        private U first;
        private V second;

        public Pair(U first, V second) {
            this.first = first;
            this.second = second;
        }

        public U getFirst() {
            return first;
        }

        public void setFirst(U first) {
            this.first = first;
        }

        public V getSecond() {
            return second;
        }

        public void setSecond(V second) {
            this.second = second;
        }
    }
}
