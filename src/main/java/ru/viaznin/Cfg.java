package ru.viaznin;

import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;

import java.util.Properties;
import java.util.function.UnaryOperator;

import static java.lang.System.getenv;

/**
 * App configuration
 *
 * @author Ilya Viaznin
 */
@Data
@Accessors(chain = true)
public class Cfg {

    /**
     * Contract address
     */
    private String viresContract;

    /**
     * Contract function name
     */
    private String viresVestingFunc;

    /**
     * Seed phrase
     */
    private String seed;

    @SneakyThrows
    public static Cfg getInstance() {
        var cfg = new Properties();
        cfg.load(Cfg.class.getClassLoader().getResourceAsStream("application.yml"));
        UnaryOperator<String> ex = cfg::getProperty;

        var seed = getenv("WAVES_SEED");
        if (seed == null) throw new IllegalArgumentException("No WAVES_SEED env!");

        return new Cfg()
                .setViresContract(ex.apply("vires-contract"))
                .setViresVestingFunc(ex.apply("vires-vesting-func"))
                .setSeed(seed);
    }
}
