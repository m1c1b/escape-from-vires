package ru.viaznin;

import com.wavesplatform.transactions.InvokeScriptTransaction;
import com.wavesplatform.transactions.account.Address;
import com.wavesplatform.transactions.account.PrivateKey;
import com.wavesplatform.transactions.invocation.Function;
import com.wavesplatform.wavesj.Node;
import com.wavesplatform.wavesj.exceptions.NodeException;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

import static com.wavesplatform.transactions.invocation.BooleanArg.as;
import static com.wavesplatform.wavesj.Profile.MAINNET;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.IntStream.rangeClosed;
import static lombok.AccessLevel.PRIVATE;
import static ru.viaznin.Cfg.getInstance;

/**
 * Common logic
 *
 * @author Ilya Viaznin
 */
@Log4j2
@NoArgsConstructor(access = PRIVATE)
public class ViresWithdraw {

    /**
     * Mainnet node
     */
    private static Node n;

    /**
     * Start
     */
    @SuppressWarnings("InfiniteLoopStatement")
    @SneakyThrows
    public static void start() {
        n = new Node(MAINNET);
        var cfg = getInstance();

        var executorPk = PrivateKey.fromSeed(cfg.getSeed());
        var viresSc = new Address(cfg.getViresContract());

        var tx = InvokeScriptTransaction
                .builder(viresSc, Function.as(cfg.getViresVestingFunc(), as(false), as(true)))
                .getSignedWith(executorPk);

        while (true) {
            var height = n.getLastBlock().height();

            // Start execution 5 blocks before and after the needed block
            var isExecutionNeededOnThisBlock = rangeClosed(-5, 5).anyMatch(i -> (i + height) % 1444 == 0);

            if (!isExecutionNeededOnThisBlock)
                log.info("Skipping execution. Current height is " + height);

            if (isExecutionNeededOnThisBlock && executeTx(tx))
                log.info("Success execution! TxId: " + tx.id());

            SECONDS.sleep(5);
        }

    }

    /**
     * Try withdrawn
     *
     * @param tx Prepared contract call
     *
     * @return Result of execution
     */
    @SneakyThrows
    private static boolean executeTx(InvokeScriptTransaction tx) {
        var result = true;

        try {
            n.waitForTransaction(n.broadcast(tx).id());
        } catch (NodeException ex) {
            result = false;
            var originalMsg = ex.getMessage();
            var humanMsg = originalMsg.split(",")[1];
            log.error(humanMsg);
        }

        return result;
    }
}