import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class assignment {
    public static class MatchInfo {
        private UUID matchId;
        private double rateA;
        private double rateB;
        private Result result;

        public MatchInfo(UUID matchId, double rateA, double rateB, String result) {
            this.matchId = matchId;
            this.rateA = rateA;
            this.rateB = rateB;
            this.result = Result.valueOf(result);
        }

        public double getRateA() {
            return rateA;
        }

        public double getRateB() {
            return rateB;
        }

        public Result getResult() {
            return result;
        }

        public UUID getMatchId() {
            return matchId;
        }
    }

    public static class Casino {
        private int balance;

        public Casino(int balance) {
            this.balance = balance;
        }

        public Casino() {
            this(0);
        }

        public void makeWithdrawal(TransactionInfo transaction) {
            int amount = transaction.getAmount();
            balance = balance - amount;
        }

        public void makeDeposit(TransactionInfo transaction) {
            int amount = transaction.getAmount();
            balance = balance - amount;
        }

        public void makeBet(TransactionInfo transaction) {
            int amount = transaction.getAmount();
            balance = balance - amount;
        }

        public int getBalance() {
            return balance;
        }

        public void addToBalance(int amount) {
            balance = balance + amount;
        }

        public void subtractFromBalance(int amount) {
            balance = balance - amount;
        }

    }

    public static class Player {
        private UUID playerId;
        private double balance;
        private double totalMatches;
        private double totalWonMatches;

        private TransactionInfo firstIllegalMove;

        public Player(UUID playerId, double balance, Map<UUID, MatchInfo> matches) {
            this.playerId = playerId;
            this.balance = balance;
            this.totalMatches = 0;
            this.totalWonMatches = 0;
            this.firstIllegalMove = null;
        }

        public Player(UUID playerId) {
            this(playerId, 0, new HashMap<>());
        }

        public UUID getPlayerId() {
            return playerId;
        }

        public double getBalance() {
            return balance;
        }

        public TransactionInfo getIllegalMove() {
            return firstIllegalMove;
        }

        public boolean isIllegalPlayer() {
            return firstIllegalMove != null;
        }

        public boolean makeBet(TransactionInfo transaction) {
            int amount = transaction.getAmount();
            if (!checkMoveValidity(transaction))
                return false;
            balance = balance - amount;
            return true;
        }

        public boolean makeDeposit(TransactionInfo transaction) {
            int amount = transaction.getAmount();
            balance = balance + amount;
            return true;
        }

        public boolean makeWithdrawal(TransactionInfo transaction) {
            int amount = transaction.getAmount();
            if (!checkMoveValidity(transaction))
                return false;
            balance = balance - amount;
            return true;
        }

        private boolean checkMoveValidity(TransactionInfo transaction) {
            int amount = transaction.getAmount();
            if (firstIllegalMove != null)
                return false;

            boolean isValid = amount <= this.balance;
            if (!isValid)
                firstIllegalMove = transaction;
            return isValid;

        }

        public void subtractFromBalance(int amount) {
            balance = balance - amount;
        }

        public void addToBalance(int amount) {
            balance = balance + amount;
        }

        public void addWin() {
            totalMatches++;
            totalWonMatches++;
        }

        public void addLose() {
            totalMatches++;
        }

        public double getWinrate() {
            return totalWonMatches / totalMatches;
        }

    }

    enum MoveType {
        DEPOSIT, WITHDRAW, BET
    }

    enum Result {
        A, B, DRAW, NO_SIDE
    }

    public static class TransactionInfo {
        private UUID playerId;
        private MoveType moveType;
        private UUID matchId;
        private int amount;
        private Result result;

        public TransactionInfo(UUID playerId, MoveType moveType, UUID matchId, int amount, Result result) {
            this.playerId = playerId;
            this.moveType = moveType;
            this.matchId = matchId;
            this.amount = amount;
            this.result = result;
        }

        public UUID getPlayerId() {
            return playerId;
        }

        public void setPlayerMatchId(UUID playerId) {
            this.playerId = playerId;
        }

        public MoveType getMoveType() {
            return moveType;
        }

        public void setMoveType(MoveType moveType) {
            this.moveType = moveType;
        }

        public UUID getMatchId() {
            return matchId;
        }

        public void setMatchId(UUID matchId) {
            this.matchId = matchId;
        }

        public int getAmount() {
            return amount;
        }

        public void setMoney(int money) {
            this.amount = money;
        }

        public Result getResult() {
            return result;
        }

        public void setResult(Result team) {
            this.result = team;
        }
    }

    public static class AssignmentController {

        public static Map<UUID, Player> PlayerMap = new HashMap<>();
        public static Map<UUID, MatchInfo> MatchMap = new HashMap<>();
        public static Casino Casino = new Casino();

        private void readMatchData(String filePath) {
            try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split(",");
                    UUID matchId = UUID.fromString(parts[0]);
                    double rateA = Double.parseDouble(parts[1]);
                    double rateB = Double.parseDouble(parts[2]);
                    String result = parts[3];

                    MatchInfo matchInfo = new MatchInfo(matchId, rateA, rateB, result);
                    MatchMap.put(matchId, matchInfo);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private Player getPlayerOrCreate(UUID playerId) {
            if (PlayerMap.containsKey(playerId)) {
                return PlayerMap.get(playerId);
            } else {
                Player player = new Player(playerId);
                PlayerMap.put(playerId, player);
                return player;
            }
        }

        private void handleWiningPlayer(TransactionInfo transactionInfo, MatchInfo matchInfo) {
            UUID playerId = transactionInfo.getPlayerId();
            Player player = getPlayerOrCreate(playerId);

            // Check if player won the bet

            int addedMoney = 0;
            if (transactionInfo.getResult() == Result.DRAW) {
                player.addWin();
                addedMoney = transactionInfo.getAmount();
            } else if (transactionInfo.getResult() == matchInfo.getResult()) {
                player.addWin();
                addedMoney = (int) (transactionInfo.getAmount() * matchInfo.getRateA());
            } else {
                player.addLose();
                addedMoney = (int) (transactionInfo.getAmount() * matchInfo.getRateB());
            }
            player.addToBalance(addedMoney);
            Casino.subtractFromBalance(addedMoney);

        }

        private void readPlayerData(String filePath) {
            try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split(",");
                    MoveType action = MoveType.valueOf(parts[1]);
                    Result side = Result.NO_SIDE;
                    UUID matchId = null;
                    if (!parts[2].equals(""))
                        matchId = UUID.fromString(parts[2]);

                    if (action == MoveType.BET) {
                        side = Result.valueOf(parts[4]);
                    }

                    UUID playerId = UUID.fromString(parts[0]);

                    int amount = Integer.parseInt(parts[3]);

                    TransactionInfo transaction = new TransactionInfo(playerId, action, matchId, amount, side);
                    Player player = getPlayerOrCreate(playerId);

                    switch (action) {
                        case DEPOSIT:

                            boolean isValidDeposit = player.makeDeposit(transaction);
                            if (isValidDeposit)
                                Casino.makeDeposit(transaction);
                            break;
                        case BET:
                            boolean isValidBet = player.makeBet(transaction);
                            if (isValidBet) {
                                Casino.makeBet(transaction);
                                handleWiningPlayer(transaction, MatchMap.get(matchId));
                            }
                            break;
                        case WITHDRAW:
                            boolean isValidWithdraw = player.makeWithdrawal(transaction);
                            if (isValidWithdraw)
                                Casino.makeWithdrawal(transaction);
                            break;
                        default:
                            System.out.println("Invalid operation: " + parts[1]);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void writeResults(String filePath) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
                // Write List of all legitimate player IDs followed with their final balance and
                // their betting win rate
                writer.println("List of legitimate players:");

                for (Map.Entry<UUID, Player> entry : PlayerMap.entrySet()) {
                    UUID playerId = entry.getKey();
                    Player player = entry.getValue();
                    writer.println(playerId + " " + player.getBalance() + " " + player.getWinrate());
                }

                // Write an empty line between result groups
                writer.println();

                // Write List of all illegitimate players represented by their first illegal
                // operation
                writer.println("List of illegitimate players:");
                for (Map.Entry<UUID, Player> entry : PlayerMap.entrySet()) {
                    UUID playerId = entry.getKey();
                    Player player = entry.getValue();
                    if (player.isIllegalPlayer()) {
                        TransactionInfo transaction = player.getIllegalMove();
                        writer.println(playerId + " " + transaction.getMoveType() + " " + transaction.getMatchId() + " "
                                + transaction.getAmount());
                    }
                }

                // Write an empty line between result groups
                writer.println();

                // Write Coin changes in casino host balance
                int casinoHostBalance = Casino.getBalance();

                writer.println();
                writer.println("Casino host balance change: " + casinoHostBalance);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static void main(String[] args) {

        AssignmentController assignmentController = new AssignmentController();

        assignmentController.readMatchData("match_data.txt");
        assignmentController.readPlayerData("player_data.txt");
        assignmentController.writeResults("result.txt");
    }
}