package com.example.myapplication.managers

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AlertDialog
import com.example.myapplication.GameButtonActivity
import com.example.myapplication.LevelSelectionActivity
import com.example.myapplication.MenuActivity
import com.example.myapplication.R
import com.example.myapplication.game.GameLogic

/**
 * 🏆 DialogManager - Quản lý các dialog trong game
 *
 * Tách riêng từ GameView để giảm độ phức tạp và dễ maintain
 */
class DialogManager(private val context: Context, private val soundManager: SoundManager) {

    // ===== DIALOG WIN =====
    fun showWinDialog(
        gameLogic: GameLogic,
        levelTime: Long,
        isNewRecord: Boolean,
        bestTime: Long?,
        onNextLevel: (Int) -> Unit,
        onDismiss: () -> Unit = {}
    ) {
        var levelId = gameLogic.getCurrentLevel()?.id ?: 1

        val dialogView = android.view.LayoutInflater.from(context).inflate(R.layout.dialog_win, null)

        val titleText = dialogView.findViewById<android.widget.TextView>(R.id.win_title)
        val messageText = dialogView.findViewById<android.widget.TextView>(R.id.win_message)
        val nextButton = dialogView.findViewById<android.widget.Button>(R.id.btn_next_level)
        val levelSelectionButton = dialogView.findViewById<android.widget.Button>(R.id.btn_back_to_level_selection)
        val menuButton = dialogView.findViewById<android.widget.Button>(R.id.btn_back_to_menu)

        // Format thời gian
        val timeFormatter = java.text.SimpleDateFormat("mm:ss", java.util.Locale.getDefault())
        timeFormatter.timeZone = java.util.TimeZone.getTimeZone("UTC")
        val currentTimeStr = timeFormatter.format(java.util.Date(levelTime))

        val titleTextStr = if (isNewRecord) "🏆 KỶ LỤC MỚI! 🏆" else "🎉 CHÚC MỪNG! 🎉"

        val messageBuilder = StringBuilder("Bạn đã hoàn thành Level $levelId!\n\n")
        messageBuilder.append("⏱️ Thời gian: $currentTimeStr\n")

        if (bestTime != null) {
            val bestTimeStr = timeFormatter.format(java.util.Date(bestTime))
            messageBuilder.append("🏆 Kỷ lục: $bestTimeStr")
            if (isNewRecord) {
                messageBuilder.append(" ⭐")
            }
        } else {
            messageBuilder.append("🏆 Kỷ lục: $currentTimeStr ⭐")
        }

        titleText.text = titleTextStr
        messageText.text = messageBuilder.toString()

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        nextButton.setOnClickListener {
            soundManager.playSound("move")
            val newLevelId = levelId + 1
            onNextLevel(newLevelId)
            dialog.dismiss()
        }

        levelSelectionButton.setOnClickListener {
            soundManager.playSound("move")
            val intent = Intent(context, LevelSelectionActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            context.startActivity(intent)
            (context as? GameButtonActivity)?.finish()
            dialog.dismiss()
        }

        menuButton.setOnClickListener {
            soundManager.playSound("move")
            val intent = Intent(context, MenuActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            context.startActivity(intent)
            (context as? GameButtonActivity)?.finish()
            dialog.dismiss()
        }

        dialog.show()
    }

    // ===== DIALOG LOSE =====
    fun showLoseDialog(
        gameLogic: GameLogic,
        onRetry: (Int) -> Unit,
        onDismiss: () -> Unit = {}
    ) {
        val levelId = gameLogic.getCurrentLevel()?.id ?: 1

        val dialogView = android.view.LayoutInflater.from(context).inflate(R.layout.dialog_lose, null)

        val titleText = dialogView.findViewById<android.widget.TextView>(R.id.lose_title)
        val messageText = dialogView.findViewById<android.widget.TextView>(R.id.lose_message)
        val retryButton = dialogView.findViewById<android.widget.Button>(R.id.btn_retry)
        val levelSelectionButton = dialogView.findViewById<android.widget.Button>(R.id.btn_back_to_level_selection)
        val menuButton = dialogView.findViewById<android.widget.Button>(R.id.btn_back_to_menu)

        titleText.text = "💀 GAME OVER! 💀"
        messageText.text = "Bạn đã thua ở Level $levelId!"

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        retryButton.setOnClickListener {
            soundManager.playSound("move")
            onRetry(levelId)
            dialog.dismiss()
        }

        levelSelectionButton.setOnClickListener {
            soundManager.playSound("move")
            val intent = Intent(context, LevelSelectionActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            context.startActivity(intent)
            (context as? GameButtonActivity)?.finish()
            dialog.dismiss()
        }

        menuButton.setOnClickListener {
            soundManager.playSound("move")
            val intent = Intent(context, MenuActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            context.startActivity(intent)
            (context as? GameButtonActivity)?.finish()
            dialog.dismiss()
        }

        dialog.show()
    }
}

