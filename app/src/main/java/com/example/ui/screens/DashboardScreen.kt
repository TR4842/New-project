package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DayProgress
import com.example.data.MilestoneProgress
import com.example.data.MistakeWord
import com.example.data.Stats
import com.example.data.UserProgress
import com.example.ui.theme.GatekeeperGold
import com.example.ui.theme.MasteredGreen
import com.example.ui.theme.RestDayBlue
import com.example.ui.theme.RestDayBlueBg

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    userProgress: UserProgress,
    dayProgressMap: Map<Int, DayProgress>,
    milestoneProgressMap: Map<Int, MilestoneProgress>,
    mistakeWords: List<MistakeWord>,
    testStats: Pair<Int, Float>,
    overallStats: Stats,
    totalDays: Int,
    onStartReadingDay: (Int) -> Unit,
    onStartDailyTest: (Int) -> Unit,
    onStartMilestoneTest: (Int) -> Unit,
    onNavigateToMistakeBank: () -> Unit,
    onDailyGoalSelected: (Int) -> Unit,
    onToggleRestDay: (Boolean) -> Unit,
    onOpenSettings: () -> Unit
) {
    val unlockedDay = userProgress.unlockedDay
    val totalAttempts = testStats.first
    val accuracy = testStats.second

    // Determine if any milestone is pending
    val completedDaysCount = dayProgressMap.values.count { it.isCompleted }
    val currentMilestoneIdx = (unlockedDay - 1) / 5 + 1
    val isPendingMilestone = (unlockedDay > 1 && (unlockedDay - 1) % 5 == 0 &&
            dayProgressMap[unlockedDay - 1]?.isCompleted == true &&
            milestoneProgressMap[(unlockedDay - 1) / 5]?.isCompleted != true)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Retro Title Banner
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "WORD SMART",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            fontFamily = FontFamily.Serif
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "The Master's Lexicon • 850 Essentials",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.testTag("settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 2. Streak & Rest Day Shield Banner
        item {
            OutlinedCard(
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("streak_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Streak info
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = "Streak",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "${userProgress.currentStreak} Day Streak",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Serif
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (userProgress.isRestDayActive) "Rest Mode Active (Frozen)" else "Active Daily Pace",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (userProgress.isRestDayActive) RestDayBlue else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Rest Day Toggle
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (userProgress.isRestDayActive) Icons.Default.Security else Icons.Default.Hotel,
                                contentDescription = "Rest Day",
                                tint = if (userProgress.isRestDayActive) RestDayBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Rest Day",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Switch(
                                checked = userProgress.isRestDayActive,
                                onCheckedChange = onToggleRestDay,
                                modifier = Modifier.testTag("rest_day_switch"),
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = RestDayBlue,
                                    checkedTrackColor = RestDayBlueBg
                                )
                            )
                        }
                    }

                    if (userProgress.isRestDayActive) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = RestDayBlueBg,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = RestDayBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Your streak is shielded today without penalty.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = RestDayBlue
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Four Key Progress Metric Tiles
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricTile(
                    modifier = Modifier.weight(1f),
                    title = "Progress",
                    value = "Day $unlockedDay",
                    subtitle = "of $totalDays days",
                    tag = "metric_progress"
                )
                MetricTile(
                    modifier = Modifier.weight(1f),
                    title = "Accuracy",
                    value = if (totalAttempts > 0) "${accuracy.toInt()}%" else "—",
                    subtitle = "$totalAttempts tests taken",
                    tag = "metric_accuracy"
                )
                MetricTile(
                    modifier = Modifier.weight(1f),
                    title = "Mastered",
                    value = "${overallStats.mastered}",
                    subtitle = "of ${overallStats.total} words",
                    tag = "metric_mastered"
                )
            }
        }

        // 4. Primary Hero Mission Card
        item {
            if (isPendingMilestone) {
                val mIdx = (unlockedDay - 1) / 5
                val mStart = (mIdx - 1) * 5 + 1
                val mEnd = mIdx * 5
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("milestone_banner")
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "5-DAY MILESTONE REVIEW",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Comprehensive review of Days $mStart–$mEnd words. Score at least 80% on this exam to unlock Day $unlockedDay!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { onStartMilestoneTest(mIdx) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("start_milestone_btn")
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Start Milestone $mIdx Exam")
                        }
                    }
                }
            } else {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("current_day_mission_card")
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "TODAY'S CHUNK",
                                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Day $unlockedDay Lexicon",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Serif
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            val isCurrentDayCompleted = dayProgressMap[unlockedDay]?.isCompleted == true
                            if (isCurrentDayCompleted) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MasteredGreen.copy(alpha = 0.15f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = MasteredGreen,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "PASSED",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MasteredGreen
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Study ${userProgress.dailyWordCount} curated words, then prove mastery on the 80% Gatekeeper Challenge.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { onStartReadingDay(unlockedDay) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("read_day_btn")
                            ) {
                                Icon(imageVector = Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Read Words")
                            }

                            OutlinedButton(
                                onClick = { onStartDailyTest(unlockedDay) },
                                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("take_test_btn")
                            ) {
                                Icon(imageVector = Icons.Default.FitnessCenter, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Take Challenge")
                            }
                        }
                    }
                }
            }
        }

        // 5. Daily Goal Selector (15, 20, 25, 30 words)
        item {
            OutlinedCard(
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Daily Word Goal",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${userProgress.dailyWordCount} words/day",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(15, 20, 25, 30).forEach { goal ->
                            val isSelected = userProgress.dailyWordCount == goal
                            FilterChip(
                                selected = isSelected,
                                onClick = { onDailyGoalSelected(goal) },
                                label = { Text("$goal words") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("goal_chip_$goal"),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }
                }
            }
        }

        // 6. Mistake Bank Quick Card
        item {
            val unresolvedMistakesCount = mistakeWords.count { !it.isResolved }
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (unresolvedMistakesCount > 0) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToMistakeBank() }
                    .testTag("mistake_bank_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.tertiaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Mistake Bank",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (unresolvedMistakesCount > 0) {
                                    "$unresolvedMistakesCount words need review"
                                } else {
                                    "No weak words recorded yet"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "Practice",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // 7. Course Roadmap Header
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ROADMAP & DAILY SETS",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "$completedDaysCount / $totalDays completed",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 8. Daily Chunks and 5-Day Milestone Interleaving
        items((1..totalDays).toList()) { day ->
            val isUnlocked = day <= unlockedDay
            val dayProg = dayProgressMap[day]
            val isCompleted = dayProg?.isCompleted == true
            val bestScore = dayProg?.bestScore ?: 0

            DayRoadmapItem(
                dayNumber = day,
                isUnlocked = isUnlocked,
                isCurrent = day == unlockedDay,
                isCompleted = isCompleted,
                bestScore = bestScore,
                onRead = { onStartReadingDay(day) },
                onTest = { onStartDailyTest(day) }
            )

            // Milestone Review Card after every 5 days
            if (day % 5 == 0) {
                val mIndex = day / 5
                val mStart = (mIndex - 1) * 5 + 1
                val mEnd = day
                val mProg = milestoneProgressMap[mIndex]
                val isMilestoneUnlocked = (1..mEnd).all { dayProgressMap[it]?.isCompleted == true }
                val isMilestoneCompleted = mProg?.isCompleted == true
                val mBestScore = mProg?.bestScore ?: 0

                MilestoneRoadmapCard(
                    milestoneIndex = mIndex,
                    startDay = mStart,
                    endDay = mEnd,
                    isUnlocked = isMilestoneUnlocked,
                    isCompleted = isMilestoneCompleted,
                    bestScore = mBestScore,
                    onStartExam = { onStartMilestoneTest(mIndex) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun MetricTile(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    tag: String
) {
    OutlinedCard(
        modifier = modifier.testTag(tag),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DayRoadmapItem(
    dayNumber: Int,
    isUnlocked: Boolean,
    isCurrent: Boolean,
    isCompleted: Boolean,
    bestScore: Int,
    onRead: () -> Unit,
    onTest: () -> Unit
) {
    val borderColor = when {
        isCurrent -> MaterialTheme.colorScheme.primary
        isCompleted -> MasteredGreen.copy(alpha = 0.6f)
        else -> MaterialTheme.colorScheme.outline
    }

    OutlinedCard(
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(if (isCurrent) 2.dp else 1.dp, borderColor),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (isUnlocked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("day_item_$dayNumber")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Day badge
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isCompleted -> MasteredGreen.copy(alpha = 0.15f)
                                isCurrent -> MaterialTheme.colorScheme.primaryContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Completed",
                            tint = MasteredGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    } else if (isUnlocked) {
                        Text(
                            text = "$dayNumber",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif
                            ),
                            color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Day $dayNumber",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Text(
                        text = when {
                            isCompleted -> "Passed: $bestScore% (80% gatekeeper met)"
                            isCurrent -> "Active Today • 80% needed to advance"
                            isUnlocked -> "Unlocked • Ready to study"
                            else -> "Locked • Complete Day ${dayNumber - 1} first"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = when {
                            isCompleted -> MasteredGreen
                            isCurrent -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            // Action Buttons
            if (isUnlocked) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                        onClick = onRead,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("read_icon_btn_$dayNumber")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = "Read Day $dayNumber",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onTest,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("test_icon_btn_$dayNumber")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = "Test Day $dayNumber",
                            tint = if (isCompleted) MasteredGreen else MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MilestoneRoadmapCard(
    milestoneIndex: Int,
    startDay: Int,
    endDay: Int,
    isUnlocked: Boolean,
    isCompleted: Boolean,
    bestScore: Int,
    onStartExam: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) {
                MasteredGreen.copy(alpha = 0.08f)
            } else if (isUnlocked) {
                GatekeeperGold.copy(alpha = 0.12f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        ),
        border = BorderStroke(
            1.5.dp,
            if (isCompleted) MasteredGreen else if (isUnlocked) GatekeeperGold else MaterialTheme.colorScheme.outline
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .testTag("milestone_card_$milestoneIndex")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.WorkspacePremium,
                    contentDescription = null,
                    tint = if (isCompleted) MasteredGreen else if (isUnlocked) GatekeeperGold else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Milestone $milestoneIndex Review (Days $startDay–$endDay)",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isCompleted) {
                            "Passed with $bestScore%! 5-Day cycle cleared."
                        } else if (isUnlocked) {
                            "Ready! 80% required to unlock Day ${endDay + 1}."
                        } else {
                            "Complete Days $startDay–$endDay tests to unlock."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (isUnlocked) {
                Button(
                    onClick = onStartExam,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCompleted) MasteredGreen else GatekeeperGold
                    ),
                    modifier = Modifier.testTag("exam_btn_$milestoneIndex")
                ) {
                    Text(if (isCompleted) "Retake" else "Exam")
                }
            }
        }
    }
}
