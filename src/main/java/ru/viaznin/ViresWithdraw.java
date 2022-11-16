package ru.viaznin;

import com.wavesplatform.transactions.InvokeScriptTransaction;
import com.wavesplatform.transactions.account.Address;
import com.wavesplatform.transactions.account.PrivateKey;
import com.wavesplatform.wavesj.Node;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

import static com.wavesplatform.transactions.InvokeScriptTransaction.builder;
import static com.wavesplatform.transactions.invocation.BooleanArg.as;
import static com.wavesplatform.transactions.invocation.Function.as;
import static com.wavesplatform.wavesj.Profile.MAINNET;
import static java.util.concurrent.TimeUnit.SECONDS;
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

        while (true) {
            var height = n.getLastBlock().height();
            log.info("Skipping execution. Current height is " + height);

            var tx =
                    builder(viresSc, as(cfg.getViresVestingFunc(), as(false), as(true)))
                            .getSignedWith(executorPk);
            if (executeTx(tx))
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
    private static boolean executeTx(InvokeScriptTransaction tx) {
        var result = true;

        try {
            n.waitForTransaction(n.broadcast(tx).id());
        } catch (Exception ex) {
            result = false;
            log.error(ex.getMessage());
        }

        return result;
    }
}
