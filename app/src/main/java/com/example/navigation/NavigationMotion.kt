package com.example.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.NavBackStackEntry

/**
 * Spécification centralisée des animations de navigation de FRANÇAIS PRO.
 *
 * Les transitions entre les cinq destinations principales (Accueil, Corriger, Apprendre,
 * Exercices, Profil) utilisent un `AnimatedContent` combinant fondu enchaîné (fade-through)
 * et glissement horizontal directionnel sur exactement 200 ms avec une courbe [FastOutSlowInEasing].
 */
object NavigationMotion {
    const val TAB_TRANSITION_DURATION_MS = 200
    const val TAB_ENTER_DURATION_MS = TAB_TRANSITION_DURATION_MS
    const val TAB_EXIT_DURATION_MS = TAB_TRANSITION_DURATION_MS
    const val TAB_ICON_DURATION_MS = 180
    const val TAB_SCALE_DURATION_MS = 200
    const val TAB_BAR_ENTER_DURATION_MS = 200
    const val TAB_BAR_EXIT_DURATION_MS = 160
    const val DRILL_IN_DURATION_MS = 200
    const val DRILL_OUT_DURATION_MS = 180

    val orderedTopLevelTabs: List<String> = listOf(
        Routes.DASHBOARD,
        Routes.CORRECTOR,
        Routes.ACADEMY,
        Routes.EXERCISES,
        Routes.PROFILE
    )

    fun tabIndexOf(route: String?): Int = orderedTopLevelTabs.indexOf(route)

    fun isTopLevelTab(route: String?): Boolean = route != null && route in Routes.topLevelTabs

    /**
     * Retourne +1 lors d'un déplacement vers un onglet à droite (ex. Accueil -> Corriger),
     * -1 vers la gauche (ex. Profil -> Exercices), et 0 hors transition inter-onglets.
     */
    fun tabSlideDirection(fromRoute: String?, toRoute: String?): Int {
        val fromIndex = tabIndexOf(fromRoute)
        val toIndex = tabIndexOf(toRoute)
        if (fromIndex == -1 || toIndex == -1 || fromIndex == toIndex) return 0
        return if (toIndex > fromIndex) 1 else -1
    }

    /**
     * Transition `AnimatedContent` (200 ms) dédiée aux cinq destinations principales :
     * combine un fondu enchaîné (fade-through) et un glissement horizontal directionnel discret.
     */
    fun mainDestinationContentTransform(
        fromRoute: String,
        toRoute: String,
        reducedMotion: Boolean
    ): ContentTransform {
        if (reducedMotion) {
            return ContentTransform(
                targetContentEnter = EnterTransition.None,
                initialContentExit = ExitTransition.None,
                sizeTransform = SizeTransform(clip = false)
            )
        }

        val direction = tabSlideDirection(fromRoute, toRoute)
        val duration = TAB_TRANSITION_DURATION_MS

        val enter = fadeIn(
            animationSpec = tween(durationMillis = duration, easing = FastOutSlowInEasing)
        ) + scaleIn(
            initialScale = 0.985f,
            animationSpec = tween(durationMillis = duration, easing = FastOutSlowInEasing)
        ) + if (direction != 0) {
            slideInHorizontally(
                animationSpec = tween(durationMillis = duration, easing = FastOutSlowInEasing),
                initialOffsetX = { fullWidth -> direction * (fullWidth / 16) }
            )
        } else {
            EnterTransition.None
        }

        val exit = fadeOut(
            animationSpec = tween(durationMillis = duration, easing = FastOutSlowInEasing)
        ) + if (direction != 0) {
            slideOutHorizontally(
                animationSpec = tween(durationMillis = duration, easing = FastOutSlowInEasing),
                targetOffsetX = { fullWidth -> -direction * (fullWidth / 20) }
            )
        } else {
            ExitTransition.None
        }

        return ContentTransform(
            targetContentEnter = enter,
            initialContentExit = exit,
            sizeTransform = SizeTransform(clip = false)
        )
    }

    fun AnimatedContentTransitionScope<NavBackStackEntry>.enterTransition(
        reducedMotion: Boolean
    ): EnterTransition {
        if (reducedMotion) return EnterTransition.None

        val fromRoute = initialState.destination.route
        if (fromRoute == Routes.SPLASH || fromRoute == Routes.ONBOARDING) {
            return fadeIn(
                animationSpec = tween(
                    durationMillis = TAB_TRANSITION_DURATION_MS,
                    easing = FastOutSlowInEasing
                )
            )
        }

        return fadeIn(
            animationSpec = tween(
                durationMillis = DRILL_IN_DURATION_MS,
                easing = FastOutSlowInEasing
            )
        ) + slideInHorizontally(
            animationSpec = tween(
                durationMillis = DRILL_IN_DURATION_MS,
                easing = FastOutSlowInEasing
            ),
            initialOffsetX = { fullWidth -> fullWidth / 12 }
        )
    }

    fun AnimatedContentTransitionScope<NavBackStackEntry>.exitTransition(
        reducedMotion: Boolean
    ): ExitTransition {
        if (reducedMotion) return ExitTransition.None

        val fromRoute = initialState.destination.route
        if (fromRoute == Routes.SPLASH || fromRoute == Routes.ONBOARDING) {
            return fadeOut(
                animationSpec = tween(
                    durationMillis = TAB_TRANSITION_DURATION_MS,
                    easing = FastOutSlowInEasing
                )
            )
        }

        return fadeOut(
            animationSpec = tween(
                durationMillis = DRILL_OUT_DURATION_MS,
                easing = FastOutSlowInEasing
            )
        ) + slideOutHorizontally(
            animationSpec = tween(
                durationMillis = DRILL_OUT_DURATION_MS,
                easing = FastOutSlowInEasing
            ),
            targetOffsetX = { fullWidth -> -fullWidth / 24 }
        )
    }

    fun AnimatedContentTransitionScope<NavBackStackEntry>.popEnterTransition(
        reducedMotion: Boolean
    ): EnterTransition {
        if (reducedMotion) return EnterTransition.None

        return fadeIn(
            animationSpec = tween(
                durationMillis = DRILL_IN_DURATION_MS,
                easing = FastOutSlowInEasing
            )
        ) + slideInHorizontally(
            animationSpec = tween(
                durationMillis = DRILL_IN_DURATION_MS,
                easing = FastOutSlowInEasing
            ),
            initialOffsetX = { fullWidth -> -fullWidth / 24 }
        )
    }

    fun AnimatedContentTransitionScope<NavBackStackEntry>.popExitTransition(
        reducedMotion: Boolean
    ): ExitTransition {
        if (reducedMotion) return ExitTransition.None

        return fadeOut(
            animationSpec = tween(
                durationMillis = DRILL_OUT_DURATION_MS,
                easing = FastOutSlowInEasing
            )
        ) + slideOutHorizontally(
            animationSpec = tween(
                durationMillis = DRILL_OUT_DURATION_MS,
                easing = FastOutSlowInEasing
            ),
            targetOffsetX = { fullWidth -> fullWidth / 12 }
        )
    }
}
