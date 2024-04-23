package updater.api.preferences;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import updater.api.metrics.MetricType;
import util.helpers.system.LoggerHelpers;

/**
 * Interface for update preferences.
 */
public interface Preferences {

    /**
     * External representation.
     */
    public String repr();

    /**
     * Print preferences.
     */
    default void print() {
        LoggerHelpers.instance().info(this.repr());
    }

    /**
     * Focus for the generation of alternative releases / change edges
     */
    public enum Focus {
        NONE, ROOT, CONSTRAINTS, ALL;
    }

    /**
     * Selectors of alternative releases
     */
    public enum Selector {
        MORE_RECENT, NO_PATCHES;
    }

    /**
     * Possible tools for direct cost computation.
     */
    public enum DirectTool {
        NONE, MARACAS;
    }

    /**
     * Possible tools for indirect cost computation
     */
    public enum IndirectTool {
        NONE, JAPICMP;
    }

    /**
     * Default cost values
     */
    public enum DefaultCost {
        MIN, MAX;

        public double toDouble() {
            return switch(this) {
                case MIN -> 0.0;
                case MAX -> 99999999.9;
            };
        }
    }

    /**
     * Get the set of quality metrics that are of interest for the user.
     * 
     * @return the set of quality metrics that are of interest for the user.
     */
    Set<MetricType> qualityMetrics();

    /**
     * Get the set of cost metrics that are of interest for the user.
     * 
     * @return the set of cost metrics that are of interest for the user.
     */
    Set<MetricType> costMetrics();

    /**
     * Get all metrics that are of interest for the user. This is the union of the
     * quality metrics and the cost metrics.
     * 
     * @return the set of all metrics that are of interest for the user.
     */
    default Set<MetricType> metrics() {
        Set<MetricType> metrics = new HashSet<>(qualityMetrics());
        metrics.addAll(costMetrics());
        return metrics;
    }

    /**
     * Get the coefficient for a given metric. Optional.empty if the metric is not in the
     * preferences (which is different from having value 0.0).
     * 
     * @param metric
     * @return
     */
    Optional<Double> coefficientFor(MetricType metric);

    /**
     * Get the sum of all coefficients for a given set of added values.
     */
    default Double sumFor(Set<MetricType> metrics) {
        return metrics.stream()
                .map(this::coefficientFor)
                .filter(Optional::isPresent)
                .mapToDouble(Optional::get)
                .sum();
    }

    /**
     * Checks whether the preferences are valid or not. Validity means that there is
     * at least one quality metric, at list one cost metric and that the sum of
     * coefficients for quality metrics is 1.
     * 
     * @return true if valid, false otherwise
     */
    default boolean isValid() {
        // 1. at least one quality metric
        if (qualityMetrics().isEmpty()) {
            LoggerHelpers.instance().error("Missing quality metrics");
            return false;
        }
        // 2. at least one cost metric
        if (costMetrics().isEmpty()) {
            LoggerHelpers.instance().error("Missing cost metrics");
            return false;
        }
        // 3. the sum of coefficients for quality metrics is 1
        if (sumFor(qualityMetrics()) != 1.0) {
            LoggerHelpers.instance().error("Quality weights sum should be 1.0");
            return false;
        }
        return true;
    }

    /**
     * Checks whether the preferences contain constraints on solutions.
     */
    boolean hasConstraints();

    /**
     * Returns the preference constraints.
     */
    List<Constraint> constraints();

    /**
     * Returns the focus for releases
     */
    Focus releaseFocus();

    /**
     * Returns the focus for change edges
     */
    Focus changeFocus();

    /*
     * Default value for costs
     */
    DefaultCost defaultCost();

    /** 
     * Returns the selectors
     */
    Set<Selector> releaseSelectors();

    /**
     * Returns the ids for focuses (
     * 
     * related to some constraints)
     */
    default Set<String> constraintFocuses() {
        return constraints().stream()
            .filter(Constraint::isFocus)
            .map(Constraint::focus)
            .collect(Collectors.toSet());
    }

    /**
     * Returns the tool used for direct cost computation.
     */
    DirectTool directTool();

    /**
     * Returns the tool used for indirect cost computation.
     */
    IndirectTool indirectTool();

}
