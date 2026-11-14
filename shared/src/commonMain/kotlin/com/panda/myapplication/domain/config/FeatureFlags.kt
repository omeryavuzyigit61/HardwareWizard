package com.panda.myapplication.domain.config

/**
 * Runtime feature toggles for phased rollout modules.
 */
object FeatureFlags {
    /**
     * Enables/disables pricing UI, currency selectors, price sorting/filters,
     * budget sliders, store offers, community price deals, price alerts, and
     * 2nd-hand valuation screens until an official retail pricing API is integrated.
     */
    const val IS_PRICING_ENABLED: Boolean = false
}
