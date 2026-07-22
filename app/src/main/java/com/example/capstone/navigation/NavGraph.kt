package com.example.capstone.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.capstone.onboarding.UserOnboardingScreen
import com.example.capstone.screen.*
import com.example.capstone.screen.article.ArticleScreen
import com.example.capstone.screen.article.ArticleDetailScreen
import com.example.capstone.screen.article.ArticleDetailScreenEs
import com.example.capstone.screen.article.ArticleDetailScreenJa
import com.example.capstone.screen.article.engPart.PrimaryNewsScreen
import com.example.capstone.screen.article.engPart.SecondaryNewsScreen
import com.example.capstone.screen.article.jaPart.PrimaryNewsScreenJa
import com.example.capstone.screen.article.jaPart.SecondaryNewsScreenJa
import com.example.capstone.screen.article.spanishPart.PrimaryNewsScreenEs
import com.example.capstone.screen.article.spanishPart.SecondaryNewsScreenEs
import com.example.capstone.viewmodel.SharedTextViewModel
import com.example.capstone.viewmodel.SharedUrlViewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.compose.ui.Modifier

@Composable
fun NavGraph(
    navController: NavHostController,
    sharedUrlViewModel: SharedUrlViewModel,
    sharedTextViewModel: SharedTextViewModel,
    profileViewModel: SharedProfileViewModel,
    isDarkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit,
    jaNewsViewModel: JaNewsViewModel, // 필요시 명시적으로 넘김
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Onboarding.route,
    ) {
        composable(Screen.Article.route) {
            ArticleScreen(
                navController = navController,
                modifier = modifier,
                isDarkMode = isDarkMode
            )
        }

        composable(Screen.WordBook.route) {
            WordBookScreen(navController, modifier = modifier)
        }

        composable(Screen.Settings.route) {
            Settings(
                isDarkMode = isDarkMode,
                onToggleDarkMode = onToggleDarkMode
            )
        }

        composable(Screen.Info.route) {
            Info()
        }

        composable(Screen.ArticleDetail.route) {
            ArticleDetailScreen(
                navController = navController,
                sharedTextViewModel = sharedTextViewModel
            )
        }

        // 영어 뉴스
        composable(Screen.PrimaryNews.route) {
            PrimaryNewsScreen(navController, sharedUrlViewModel, sharedTextViewModel)
        }
        composable(Screen.SecondaryNews.route) {
            SecondaryNewsScreen(navController, sharedUrlViewModel, sharedTextViewModel)
        }

        // 일본어 뉴스
        composable(Screen.PrimaryNewsJa.route) {
            PrimaryNewsScreenJa(navController, sharedUrlViewModel, sharedTextViewModel)
        }
        composable(Screen.SecondaryNewsJa.route) {
            SecondaryNewsScreenJa(navController, sharedUrlViewModel, sharedTextViewModel)
        }
        composable(Screen.ArticleDetailJa.route) {
            ArticleDetailScreenJa(sharedUrlViewModel, sharedTextViewModel)
        }

        // 스페인어 뉴스
        composable(Screen.PrimaryNewsEs.route) {
            PrimaryNewsScreenEs(navController, sharedUrlViewModel, sharedTextViewModel)
        }
        composable(Screen.SecondaryNewsEs.route) {
            SecondaryNewsScreenEs(navController, sharedUrlViewModel, sharedTextViewModel)
        }
        composable(Screen.ArticleDetailEs.route) {
            ArticleDetailScreenEs(sharedUrlViewModel, sharedTextViewModel)
        }

        // 퀴즈
        composable(
            route = "quiz/{langCode}",
            arguments = listOf(navArgument("langCode") { type = NavType.StringType })
        ) { backStackEntry ->
            val langCode = backStackEntry.arguments?.getString("langCode") ?: "en"
            QuizScreen(navController, langCode, modifier)
        }

        // 오답노트
        composable(
            route = "wrong_note/{langCode}",
            arguments = listOf(navArgument("langCode") { type = NavType.StringType })
        ) { backStackEntry ->
            val langCode = backStackEntry.arguments?.getString("langCode") ?: "en"
            WrongNoteScreen(navController, langCode, modifier)
        }

        // 온보딩
        composable(Screen.Onboarding.route) {
            UserOnboardingScreen(
                navController = navController,
                profileViewModel = profileViewModel
            )
        }

        // 프로필
        composable(Screen.Profile.route) {
            ProfileScreen(
                navController = navController,
                profileViewModel = profileViewModel
            )
        }
    }
}
