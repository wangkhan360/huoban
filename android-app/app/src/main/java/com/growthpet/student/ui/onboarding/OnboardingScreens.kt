package com.growthpet.student.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.growthpet.student.R
import com.growthpet.student.companion.CompanionState
import com.growthpet.student.companion.YunlongCompanionStage
import com.growthpet.student.ui.theme.*
import kotlinx.coroutines.delay

enum class OnboardingStep(val index: Int, val title: String) {
    SPLASH(0, "启动页"),
    WELCOME(1, "云龙欢迎页"),
    PARENT_CODE(2, "家长核验码"),
    IDENTITY_CONFIRM(3, "孩子身份确认"),
    YUNLONG_REVEAL(4, "云龙正式亮相"),
    NAME_YUNLONG(5, "给云龙起名字"),
    FIRST_PORTRAIT_CHAT(6, "AI 初次认识对话"),
    PORTRAIT_CONFIRM(7, "成长画像确认")
}

data class PortraitQuestion(
    val key: String,
    val prompt: String,
    val isSensitive: Boolean,
    val quickOptions: List<String>
)

private val portraitQuestions = listOf(
    PortraitQuestion("joy", "平时什么事情最容易让你开心呀？", false, listOf("和好朋友一起玩", "拼乐高/做手工", "看科普绘本")),
    PortraitQuestion("hobbies", "你最喜欢做的兴趣爱好是什么？", false, listOf("画画涂鸦", "观察天文科学", "踢足球/跳绳")),
    PortraitQuestion("likes_dislikes", "有没有特别喜欢或不太喜欢的事情？", false, listOf("喜欢动手实验，不喜欢抄写太多", "喜欢听故事，不喜欢太吵")),
    PortraitQuestion("worries", "遇到什么事情时会有点害怕或担心？（可跳过）", true, listOf("怕考不好被批评", "怕黑/不敢一个人睡", "暂无特别担心的")),
    PortraitQuestion("aspiration", "长大以后，你想成为一个怎样的人？", false, listOf("科学家/发明家", "飞行员/宇航员", "老师/医生")),
    PortraitQuestion("role_model", "你最崇拜或欣赏的榜样是谁呢？", false, listOf("钱学森", "袁隆平", "航天员叔叔阿姨")),
    PortraitQuestion("wish", "现在心里最想实现的一个小愿望是什么？", false, listOf("拥有一套天文望远镜", "周末去科技馆玩")),
    PortraitQuestion("weekday_fun", "平时放学后最喜欢做什么放松一下？", false, listOf("画一会画", "听故事/听音乐", "下楼跑一跑")),
    PortraitQuestion("weekend_fun", "周末最期待全家一起做什么活动？", false, listOf("去公园野餐露营", "参观博物馆", "在家一起做饭"))
)

@Composable
fun OnboardingFlowScreen(
    onVerifyCode: (
        code: String,
        onOk: (childId: String, childName: String, gradeLevel: Int, gradeBand: String, parentName: String) -> Unit,
        onErr: (String) -> Unit
    ) -> Unit,
    onCompleteOnboarding: (
        childId: String,
        childName: String,
        gradeLevel: Int,
        gradeBand: String,
        parentName: String,
        companionName: String,
        portraitJson: String
    ) -> Unit
) {
    var step by remember { mutableStateOf(OnboardingStep.SPLASH) }
    var verificationCode by remember { mutableStateOf("1012") }
    var codeError by remember { mutableStateOf<String?>(null) }

    var childId by remember { mutableStateOf("child_grade1_linlin") }
    var childName by remember { mutableStateOf("乐乐") }
    var gradeLevel by remember { mutableIntStateOf(1) }
    var gradeBand by remember { mutableStateOf("LOWER_GRADE") }
    var parentName by remember { mutableStateOf("乐乐爸爸") }

    var companionName by remember { mutableStateOf("云龙") }
    var questionIndex by remember { mutableIntStateOf(0) }
    var draftAnswer by remember { mutableStateOf("") }
    val portraitAnswers = remember { mutableStateMapOf<String, String>() }

    LaunchedEffect(step) {
        if (step == OnboardingStep.SPLASH) {
            delay(1300)
            step = OnboardingStep.WELCOME
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(ColorTokens.SkyGradientTop, ColorTokens.CanvasIce, ColorTokens.SurfaceCard)
                )
            )
            .systemBarsPadding()
    ) {
        AnimatedContent(targetState = step, label = "onboarding_step") { current ->
            when (current) {
                OnboardingStep.SPLASH -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(Spacing.xl),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_yunlong_app_icon),
                            contentDescription = "成长萌宠图标",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(96.dp).clip(RoundedCornerShape(Radius.xl))
                        )
                        Spacer(modifier = Modifier.height(Spacing.lg))
                        Text("成长萌宠", style = MaterialTheme.typography.headlineLarge)
                        Spacer(modifier = Modifier.height(Spacing.xs))
                        Text(
                            "儿童 AI 成长伙伴 · 朋友 / 管家 / 教练",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ColorTokens.InkSecondary
                        )
                    }
                }

                OnboardingStep.WELCOME -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(Spacing.xl),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Spacer(modifier = Modifier.height(Spacing.md))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            YunlongCompanionStage(
                                state = CompanionState.HAPPY,
                                companionName = "云龙",
                                isLowerGrade = true,
                                size = 220.dp
                            )
                            Spacer(modifier = Modifier.height(Spacing.lg))
                            Text(
                                "你好呀！我是来自云端的成长伙伴云龙",
                                style = MaterialTheme.typography.headlineSmall,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(Spacing.sm))
                            Text(
                                "我会用朋友的方式陪你聊天，用管家的办法帮你理清任务，用教练的耐心陪你实现小目标。",
                                style = MaterialTheme.typography.bodyLarge,
                                color = ColorTokens.InkSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                        Button(
                            onClick = { step = OnboardingStep.PARENT_CODE },
                            shape = ButtonStyle.shape,
                            modifier = Modifier.fillMaxWidth().height(ButtonStyle.heightPrimary)
                        ) {
                            Text("开始绑定并认识云龙")
                        }
                    }
                }

                OnboardingStep.PARENT_CODE -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(Spacing.xl),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("步骤 1 / 5 · 家庭安全绑定", style = MaterialTheme.typography.labelMedium)
                            Spacer(modifier = Modifier.height(Spacing.xs))
                            Text("输入家长核验码", style = MaterialTheme.typography.headlineSmall)
                            Spacer(modifier = Modifier.height(Spacing.xs))
                            Text(
                                "请向爸爸或妈妈获取 4 位家庭核验码（本地模拟演示：输入 1012 为一年级乐乐，输入 6068 为六年级思远）。",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(Spacing.lg))
                            OutlinedTextField(
                                value = verificationCode,
                                onValueChange = {
                                    verificationCode = it
                                    codeError = null
                                },
                                label = { Text("家长核验码") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(Radius.md)
                            )
                            if (codeError != null) {
                                Spacer(modifier = Modifier.height(Spacing.xs))
                                Text(codeError!!, color = ColorTokens.DangerCoral, style = MaterialTheme.typography.bodyMedium)
                            }
                            Spacer(modifier = Modifier.height(Spacing.sm))
                            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                                OutlinedButton(onClick = { verificationCode = "1012" }, modifier = Modifier.height(44.dp)) {
                                    Text("填入一年级码 1012")
                                }
                                OutlinedButton(onClick = { verificationCode = "6068" }, modifier = Modifier.height(44.dp)) {
                                    Text("填入六年级码 6068")
                                }
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                            OutlinedButton(
                                onClick = { step = OnboardingStep.WELCOME },
                                modifier = Modifier.weight(1f).height(ButtonStyle.heightPrimary),
                                shape = ButtonStyle.shape
                            ) { Text("返回") }
                            Button(
                                onClick = {
                                    onVerifyCode(
                                        verificationCode,
                                        { cid, cname, gLevel, gBand, pName ->
                                            childId = cid
                                            childName = cname
                                            gradeLevel = gLevel
                                            gradeBand = gBand
                                            parentName = pName
                                            step = OnboardingStep.IDENTITY_CONFIRM
                                        },
                                        { err -> codeError = err }
                                    )
                                },
                                modifier = Modifier.weight(2f).height(ButtonStyle.heightPrimary),
                                shape = ButtonStyle.shape
                            ) { Text("验证并继续") }
                        }
                    }
                }

                OnboardingStep.IDENTITY_CONFIRM -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(Spacing.xl),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("步骤 2 / 5 · 身份确认", style = MaterialTheme.typography.labelMedium)
                            Spacer(modifier = Modifier.height(Spacing.xs))
                            Text("请确认是不是你本人", style = MaterialTheme.typography.headlineSmall)
                            Spacer(modifier = Modifier.height(Spacing.lg))
                            Surface(
                                shape = CardStyle.shape,
                                border = CardStyle.border,
                                color = ColorTokens.SurfaceCard,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(Spacing.lg)) {
                                    Text(childName, style = MaterialTheme.typography.headlineSmall)
                                    Spacer(modifier = Modifier.height(Spacing.xxs))
                                    Text(
                                        "${gradeLevel}年级 · ${if (gradeBand == "LOWER_GRADE") "低年级友好引导模式" else "高年级自主规划模式"}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Spacer(modifier = Modifier.height(Spacing.sm))
                                    HorizontalDivider(color = ColorTokens.BorderHairline)
                                    Spacer(modifier = Modifier.height(Spacing.sm))
                                    Text("绑定家长：$parentName", style = MaterialTheme.typography.bodyMedium)
                                    Text("设备账号严格隔离，不与其他孩子交叉访问", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                            OutlinedButton(
                                onClick = { step = OnboardingStep.PARENT_CODE },
                                modifier = Modifier.weight(1f).height(ButtonStyle.heightPrimary),
                                shape = ButtonStyle.shape
                            ) { Text("重新输入") }
                            Button(
                                onClick = { step = OnboardingStep.YUNLONG_REVEAL },
                                modifier = Modifier.weight(2f).height(ButtonStyle.heightPrimary),
                                shape = ButtonStyle.shape
                            ) { Text("是我，确认身份") }
                        }
                    }
                }

                OnboardingStep.YUNLONG_REVEAL -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(Spacing.xl),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("步骤 3 / 5 · 伙伴登场", style = MaterialTheme.typography.labelMedium)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            YunlongCompanionStage(
                                state = CompanionState.CELEBRATING,
                                companionName = "云龙",
                                isLowerGrade = gradeBand == "LOWER_GRADE",
                                size = 230.dp
                            )
                            Spacer(modifier = Modifier.height(Spacing.md))
                            Text("云龙正式与你见面啦！", style = MaterialTheme.typography.headlineSmall)
                            Spacer(modifier = Modifier.height(Spacing.xs))
                            Text(
                                "从今天起，云龙就是你在手机里的专属成长伙伴。不管做作业、定目标还是聊心事，我都一直陪着你。",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                        Button(
                            onClick = { step = OnboardingStep.NAME_YUNLONG },
                            modifier = Modifier.fillMaxWidth().height(ButtonStyle.heightPrimary),
                            shape = ButtonStyle.shape
                        ) { Text("给云龙起一个专属名字") }
                    }
                }

                OnboardingStep.NAME_YUNLONG -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(Spacing.xl),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("步骤 4 / 5 · 专属称呼", style = MaterialTheme.typography.labelMedium)
                            Spacer(modifier = Modifier.height(Spacing.xs))
                            Text("你想叫云龙什么名字？", style = MaterialTheme.typography.headlineSmall)
                            Spacer(modifier = Modifier.height(Spacing.md))
                            OutlinedTextField(
                                value = companionName,
                                onValueChange = { companionName = it },
                                label = { Text("伙伴昵称") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(Radius.md)
                            )
                            Spacer(modifier = Modifier.height(Spacing.sm))
                            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                                listOf("云龙", "小云朵", "蓝风", "星云").forEach { preset ->
                                    OutlinedButton(
                                        onClick = { companionName = preset },
                                        modifier = Modifier.height(44.dp)
                                    ) { Text(preset) }
                                }
                            }
                        }
                        Button(
                            onClick = {
                                if (companionName.isBlank()) companionName = "云龙"
                                step = OnboardingStep.FIRST_PORTRAIT_CHAT
                            },
                            modifier = Modifier.fillMaxWidth().height(ButtonStyle.heightPrimary),
                            shape = ButtonStyle.shape
                        ) { Text("叫它「${companionName.ifBlank { "云龙" }}」，开始聊几句") }
                    }
                }

                OnboardingStep.FIRST_PORTRAIT_CHAT -> {
                    val q = portraitQuestions[questionIndex]
                    Column(
                        modifier = Modifier.fillMaxSize().padding(Spacing.xl),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                            Text(
                                "初次认识 ${questionIndex + 1} / ${portraitQuestions.size}",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Spacer(modifier = Modifier.height(Spacing.sm))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(48.dp).clip(CircleShape).background(ColorTokens.SkyHaloLight)
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.img_yunlong_listening),
                                        contentDescription = companionName,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Spacer(modifier = Modifier.width(Spacing.sm))
                                Surface(
                                    shape = CardStyle.shape,
                                    border = CardStyle.border,
                                    color = ColorTokens.SurfaceCard,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = q.prompt,
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.padding(Spacing.md)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(Spacing.lg))
                            Text("点选快速回答，或者自己输入：", style = MaterialTheme.typography.labelMedium)
                            Spacer(modifier = Modifier.height(Spacing.xs))
                            q.quickOptions.forEach { opt ->
                                OutlinedButton(
                                    onClick = { draftAnswer = opt },
                                    border = BorderStroke(
                                        1.dp,
                                        if (draftAnswer == opt) ColorTokens.TechBluePrimary else ColorTokens.BorderHairline
                                    ),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).height(46.dp),
                                    shape = RoundedCornerShape(Radius.md)
                                ) {
                                    Text(opt, modifier = Modifier.fillMaxWidth())
                                }
                            }
                            Spacer(modifier = Modifier.height(Spacing.sm))
                            OutlinedTextField(
                                value = draftAnswer,
                                onValueChange = { draftAnswer = it },
                                label = { Text("也可以在这里写下你的回答") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(Radius.md)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                            if (q.isSensitive) {
                                OutlinedButton(
                                    onClick = {
                                        portraitAnswers[q.key] = "已跳过"
                                        draftAnswer = ""
                                        if (questionIndex < portraitQuestions.lastIndex) questionIndex++
                                        else step = OnboardingStep.PORTRAIT_CONFIRM
                                    },
                                    modifier = Modifier.weight(1f).height(ButtonStyle.heightPrimary),
                                    shape = ButtonStyle.shape
                                ) { Text("跳过此题") }
                            }
                            Button(
                                onClick = {
                                    portraitAnswers[q.key] = draftAnswer.ifBlank { q.quickOptions.first() }
                                    draftAnswer = ""
                                    if (questionIndex < portraitQuestions.lastIndex) questionIndex++
                                    else step = OnboardingStep.PORTRAIT_CONFIRM
                                },
                                modifier = Modifier.weight(2f).height(ButtonStyle.heightPrimary),
                                shape = ButtonStyle.shape
                            ) {
                                Text(if (questionIndex < portraitQuestions.lastIndex) "下一题" else "生成我的成长画像")
                            }
                        }
                    }
                }

                OnboardingStep.PORTRAIT_CONFIRM -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(Spacing.xl),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                            Text("步骤 5 / 5 · 画像确认", style = MaterialTheme.typography.labelMedium)
                            Spacer(modifier = Modifier.height(Spacing.xs))
                            Text("${childName}的成长伙伴档案", style = MaterialTheme.typography.headlineSmall)
                            Spacer(modifier = Modifier.height(Spacing.md))
                            Surface(
                                shape = CardStyle.shape,
                                border = CardStyle.border,
                                color = ColorTokens.SurfaceCard,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(Spacing.md), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                                    portraitQuestions.forEach { q ->
                                        val ans = portraitAnswers[q.key] ?: "未填写"
                                        Text("${q.prompt}", style = MaterialTheme.typography.labelMedium)
                                        Text(ans, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                        HorizontalDivider(color = ColorTokens.BorderHairline)
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(Spacing.md))
                        Button(
                            onClick = {
                                val summary = portraitAnswers.entries.joinToString(";") { "${it.key}=${it.value}" }
                                onCompleteOnboarding(
                                    childId, childName, gradeLevel, gradeBand, parentName, companionName, summary
                                )
                            },
                            modifier = Modifier.fillMaxWidth().height(ButtonStyle.heightPrimary),
                            shape = ButtonStyle.shape
                        ) {
                            Text("确认画像，进入与${companionName}的主对话")
                        }
                    }
                }
            }
        }
    }
}
