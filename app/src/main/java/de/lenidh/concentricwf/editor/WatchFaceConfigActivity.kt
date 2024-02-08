/*
 * Copyright (c) 2024 Moritz Heindl
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package de.lenidh.concentricwf.editor

import android.content.Intent
import android.content.res.Resources
import android.graphics.Color.parseColor
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.wear.compose.foundation.lazy.AutoCenteringParams
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListAnchorType
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.CompactChip
import androidx.wear.compose.material.HorizontalPageIndicator
import androidx.wear.compose.material.ListHeader
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PageIndicatorState
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import androidx.wear.compose.ui.tooling.preview.WearPreviewLargeRound
import androidx.wear.watchface.style.UserStyleSetting
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import de.lenidh.concentricwf.BuildConfig
import de.lenidh.concentricwf.R
import de.lenidh.concentricwf.data.editor.LicenseInfo
import de.lenidh.concentricwf.data.editor.SELF_LICENSE_INFO
import de.lenidh.concentricwf.data.editor.TP_LICENSE_INFOS
import de.lenidh.concentricwf.data.watchface.COLOR_OPTIONS
import de.lenidh.concentricwf.data.watchface.FONT_OPTIONS
import de.lenidh.concentricwf.data.watchface.WatchFaceUserStyle
import de.lenidh.concentricwf.utils.COMPLICATION_1_BOTTOM_BOUND
import de.lenidh.concentricwf.utils.COMPLICATION_1_ID
import de.lenidh.concentricwf.utils.COMPLICATION_1_LEFT_BOUND
import de.lenidh.concentricwf.utils.COMPLICATION_1_RIGHT_BOUND
import de.lenidh.concentricwf.utils.COMPLICATION_1_TOP_BOUND
import de.lenidh.concentricwf.utils.COMPLICATION_2_BOTTOM_BOUND
import de.lenidh.concentricwf.utils.COMPLICATION_2_ID
import de.lenidh.concentricwf.utils.COMPLICATION_2_LEFT_BOUND
import de.lenidh.concentricwf.utils.COMPLICATION_2_RIGHT_BOUND
import de.lenidh.concentricwf.utils.COMPLICATION_2_TOP_BOUND
import de.lenidh.concentricwf.utils.COMPLICATION_3_BOTTOM_BOUND
import de.lenidh.concentricwf.utils.COMPLICATION_3_ID
import de.lenidh.concentricwf.utils.COMPLICATION_3_LEFT_BOUND
import de.lenidh.concentricwf.utils.COMPLICATION_3_RIGHT_BOUND
import de.lenidh.concentricwf.utils.COMPLICATION_3_TOP_BOUND
import de.lenidh.concentricwf.utils.COMPLICATION_4_BOTTOM_BOUND
import de.lenidh.concentricwf.utils.COMPLICATION_4_ID
import de.lenidh.concentricwf.utils.COMPLICATION_4_LEFT_BOUND
import de.lenidh.concentricwf.utils.COMPLICATION_4_RIGHT_BOUND
import de.lenidh.concentricwf.utils.COMPLICATION_4_TOP_BOUND
import de.lenidh.concentricwf.utils.COMPLICATION_5_BOTTOM_BOUND
import de.lenidh.concentricwf.utils.COMPLICATION_5_ID
import de.lenidh.concentricwf.utils.COMPLICATION_5_LEFT_BOUND
import de.lenidh.concentricwf.utils.COMPLICATION_5_RIGHT_BOUND
import de.lenidh.concentricwf.utils.COMPLICATION_5_TOP_BOUND
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Allows user to edit certain parts of the watch face (color style, complications etc.) by using
 * the [WatchFaceConfigStateHolder]. (All widgets are disabled until data is loaded.)
 */
class WatchFaceConfigActivity : ComponentActivity() {
    private val stateHolder: WatchFaceConfigStateHolder by lazy {
        WatchFaceConfigStateHolder(
            lifecycleScope, this@WatchFaceConfigActivity
        )
    }

    private lateinit var navController: NavHostController
    private lateinit var preview: MutableState<ImageBitmap?>
    private lateinit var currentColorId: MutableState<String>
    private lateinit var currentFontId: MutableState<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate()")

        preview = mutableStateOf(ImageBitmap.imageResource(resources, R.drawable.watch_preview))
        currentColorId = mutableStateOf("")
        currentFontId = mutableStateOf("")

        setTheme(android.R.style.Theme_DeviceDefault)

        val colorOptions = WatchFaceUserStyle.getColorOptionList(this)
        val fontOptions = WatchFaceUserStyle.getFontOptionList(this)

        setContent {
            navController = rememberSwipeDismissableNavController()

            MaterialTheme {
                Editor(preview = preview,
                    colorOptions = colorOptions,
                    fontOptions = fontOptions,
                    currentColorId = currentColorId,
                    currentFontId = currentFontId,
                    onColorSelected = { id ->
                        stateHolder.setColorStyle(id)
                    },
                    onFontSelected = { id ->
                        stateHolder.setFontStyle(id)
                    },
                    onClick = { id ->
                        stateHolder.setComplication(id)
                    })
            }
        }

        lifecycleScope.launch(Dispatchers.Main.immediate) {
            stateHolder.uiState.collect { uiState: WatchFaceConfigStateHolder.EditWatchFaceUiState ->
                when (uiState) {
                    is WatchFaceConfigStateHolder.EditWatchFaceUiState.Loading -> {
                        Log.d(TAG, "StateFlow Loading: ${uiState.message}")
                    }

                    is WatchFaceConfigStateHolder.EditWatchFaceUiState.Success -> {
                        Log.d(TAG, "StateFlow Success.")
                        updateWatchFaceEditorPreview(uiState.userStylesAndPreview)
                        currentColorId.value = uiState.userStylesAndPreview.colorStyleId
                        currentFontId.value = uiState.userStylesAndPreview.fontStyleId
                    }

                    is WatchFaceConfigStateHolder.EditWatchFaceUiState.Error -> {
                        Log.e(TAG, "Flow error: ${uiState.exception}")
                    }
                }
            }
        }
    }

    private fun updateWatchFaceEditorPreview(
        userStylesAndPreview: WatchFaceConfigStateHolder.UserStylesAndPreview
    ) {
        Log.d(TAG, "updateWatchFacePreview: $userStylesAndPreview")

        val colorStyleId: String = userStylesAndPreview.colorStyleId
        Log.d(TAG, "\tselected color style: $colorStyleId")

        val fontStyleId: String = userStylesAndPreview.fontStyleId
        Log.d(TAG, "\tselected font style: $fontStyleId")

        preview.value = userStylesAndPreview.previewImage.asImageBitmap()
    }

    companion object {
        const val TAG = "WatchFaceConfigActivity"
    }
}

@Composable
private fun Editor(
    navController: NavHostController = rememberSwipeDismissableNavController(),
    preview: MutableState<ImageBitmap?> = mutableStateOf(null),
    colorOptions: List<UserStyleSetting.ListUserStyleSetting.ListOption> = emptyList(),
    fontOptions: List<UserStyleSetting.ListUserStyleSetting.ListOption> = emptyList(),
    currentColorId: MutableState<String> = mutableStateOf(COLOR_OPTIONS[0].id),
    currentFontId: MutableState<String> = mutableStateOf(FONT_OPTIONS[0].id),
    onClick: (Int) -> Unit = {},
    onColorSelected: (String) -> Unit = {},
    onFontSelected: (String) -> Unit = {},
) {
    SwipeDismissableNavHost(
        navController = navController, startDestination = "editor"
    ) {
        composable(route = "editor") {
            OptionPager(
                navController = navController,
                preview = preview,
                currentColorId = currentColorId,
                currentFontId = currentFontId,
                onClick = onClick,
            )
        }
        composable(route = "editor/color") {
            ColorPicker(colorOptions) { id ->
                navController.popBackStack()
                onColorSelected(id)
            }
        }
        composable(route = "editor/font") {
            FontPicker(fontOptions) { id ->
                navController.popBackStack()
                onFontSelected(id)
            }
        }
        composable(route = "info/licenses/self") {
            LicenseTextScreen(SELF_LICENSE_INFO)
        }
        composable(route = "info/licenses/artwork") {
            LicenseListScreen(navController)
        }
        composable(
            route = "info/licenses/artwork/{i}",
            arguments = listOf(navArgument("i") { type = NavType.IntType })
        ) { backStackEntry ->
            backStackEntry.arguments?.let {
                ArtworkLicenseTextScreen(it.getInt("i"))
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@WearPreviewLargeRound
@Composable
private fun OptionPager(
    navController: NavHostController = rememberSwipeDismissableNavController(),
    preview: MutableState<ImageBitmap?> = mutableStateOf(null),
    currentColorId: MutableState<String> = mutableStateOf(COLOR_OPTIONS[0].id),
    currentFontId: MutableState<String> = mutableStateOf(FONT_OPTIONS[0].id),
    onClick: (Int) -> Unit = {}
) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val pageIndicatorState = remember {
        object : PageIndicatorState {
            override val pageOffset: Float
                get() = pagerState.currentPageOffsetFraction
            override val selectedPage: Int
                get() = pagerState.currentPage
            override val pageCount: Int
                get() = pagerState.pageCount
        }
    }

    Scaffold(pageIndicator = {
        HorizontalPageIndicator(
            pageIndicatorState = pageIndicatorState, modifier = Modifier.padding(6.dp)
        )
    }) {
        val previewImage by remember { preview }
        previewImage?.let {
            Image(
                bitmap = it, contentDescription = "", modifier = Modifier.fillMaxSize()
            )
        }
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            pageSize = PageSize.Fill,
        ) { page ->
            if (page == 0) {
                val optionId by remember { currentColorId }
                val selectedOption = WatchFaceUserStyle.getColorOption(optionId)
                val text = selectedOption?.let { stringResource(it.nameId) } ?: optionId
                OptionPage(
                    labelText = stringResource(R.string.colors_style_setting),
                    valueText = text,
                    onStartPicker = { navController.navigate("editor/color") })
            }
            if (page == 1) {
                val optionId by remember { currentFontId }
                val selectedOption = WatchFaceUserStyle.getFontOption(optionId)
                val text = selectedOption?.let { stringResource(it.nameId) } ?: "???"
                OptionPage(
                    labelText = stringResource(R.string.fonts_style_setting),
                    valueText = text,
                    onStartPicker = { navController.navigate("editor/font") })
            }
            if (page == 2) {
                ConstraintLayout(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    val complication1LeftGuideline = createGuidelineFromAbsoluteLeft(
                        COMPLICATION_1_LEFT_BOUND
                    )
                    val complication1RightGuideline = createGuidelineFromAbsoluteLeft(
                        COMPLICATION_1_RIGHT_BOUND
                    )
                    val complication1TopGuideline = createGuidelineFromTop(
                        COMPLICATION_1_TOP_BOUND
                    )
                    val complication1BottomGuideline = createGuidelineFromTop(
                        COMPLICATION_1_BOTTOM_BOUND
                    )
                    val complication2LeftGuideline = createGuidelineFromAbsoluteLeft(
                        COMPLICATION_2_LEFT_BOUND
                    )
                    val complication2RightGuideline = createGuidelineFromAbsoluteLeft(
                        COMPLICATION_2_RIGHT_BOUND
                    )
                    val complication2TopGuideline = createGuidelineFromTop(
                        COMPLICATION_2_TOP_BOUND
                    )
                    val complication2BottomGuideline = createGuidelineFromTop(
                        COMPLICATION_2_BOTTOM_BOUND
                    )
                    val complication3LeftGuideline = createGuidelineFromAbsoluteLeft(
                        COMPLICATION_3_LEFT_BOUND
                    )
                    val complication3RightGuideline = createGuidelineFromAbsoluteLeft(
                        COMPLICATION_3_RIGHT_BOUND
                    )
                    val complication3TopGuideline = createGuidelineFromTop(
                        COMPLICATION_3_TOP_BOUND
                    )
                    val complication3BottomGuideline = createGuidelineFromTop(
                        COMPLICATION_3_BOTTOM_BOUND
                    )
                    val complication4LeftGuideline = createGuidelineFromAbsoluteLeft(
                        COMPLICATION_4_LEFT_BOUND
                    )
                    val complication4RightGuideline = createGuidelineFromAbsoluteLeft(
                        COMPLICATION_4_RIGHT_BOUND
                    )
                    val complication4TopGuideline = createGuidelineFromTop(
                        COMPLICATION_4_TOP_BOUND
                    )
                    val complication4BottomGuideline = createGuidelineFromTop(
                        COMPLICATION_4_BOTTOM_BOUND
                    )
                    val complication5LeftGuideline = createGuidelineFromAbsoluteLeft(
                        COMPLICATION_5_LEFT_BOUND
                    )
                    val complication5RightGuideline = createGuidelineFromAbsoluteLeft(
                        COMPLICATION_5_RIGHT_BOUND
                    )
                    val complication5TopGuideline = createGuidelineFromTop(
                        COMPLICATION_5_TOP_BOUND
                    )
                    val complication5BottomGuideline = createGuidelineFromTop(
                        COMPLICATION_5_BOTTOM_BOUND
                    )

                    val (button1, button2, button3, button4, button5) = createRefs()

                    Button(modifier = Modifier.constrainAs(button1) {
                        absoluteLeft.linkTo(complication1LeftGuideline)
                        absoluteRight.linkTo(complication1RightGuideline)
                        top.linkTo(complication1TopGuideline)
                        bottom.linkTo(complication1BottomGuideline)
                    },
                        colors = ButtonDefaults.outlinedButtonColors(),
                        border = ButtonDefaults.outlinedButtonBorder(
                            borderColor = Color.Gray, borderWidth = 3.dp
                        ),
                        onClick = { onClick(COMPLICATION_1_ID) }) {}
                    Button(modifier = Modifier.constrainAs(button2) {
                        absoluteLeft.linkTo(complication2LeftGuideline)
                        absoluteRight.linkTo(complication2RightGuideline)
                        top.linkTo(complication2TopGuideline)
                        bottom.linkTo(complication2BottomGuideline)
                    },
                        colors = ButtonDefaults.outlinedButtonColors(),
                        border = ButtonDefaults.outlinedButtonBorder(
                            borderColor = Color.Gray, borderWidth = 3.dp
                        ),
                        onClick = { onClick(COMPLICATION_2_ID) }) {}
                    Button(modifier = Modifier.constrainAs(button3) {
                        absoluteLeft.linkTo(complication3LeftGuideline)
                        absoluteRight.linkTo(complication3RightGuideline)
                        top.linkTo(complication3TopGuideline)
                        bottom.linkTo(complication3BottomGuideline)
                    },
                        colors = ButtonDefaults.outlinedButtonColors(),
                        border = ButtonDefaults.outlinedButtonBorder(
                            borderColor = Color.Gray, borderWidth = 3.dp
                        ),
                        onClick = { onClick(COMPLICATION_3_ID) }) {}
                    Button(modifier = Modifier.constrainAs(button4) {
                        absoluteLeft.linkTo(complication4LeftGuideline)
                        absoluteRight.linkTo(complication4RightGuideline)
                        top.linkTo(complication4TopGuideline)
                        bottom.linkTo(complication4BottomGuideline)
                    },
                        colors = ButtonDefaults.outlinedButtonColors(),
                        border = ButtonDefaults.outlinedButtonBorder(
                            borderColor = Color.Gray, borderWidth = 3.dp
                        ),
                        onClick = { onClick(COMPLICATION_4_ID) }) {}
                    Button(
                        modifier = Modifier.constrainAs(button5) {
                            absoluteLeft.linkTo(complication5LeftGuideline)
                            absoluteRight.linkTo(complication5RightGuideline)
                            top.linkTo(complication5TopGuideline)
                            bottom.linkTo(complication5BottomGuideline)
                        },
                        colors = ButtonDefaults.outlinedButtonColors(),
                        border = ButtonDefaults.outlinedButtonBorder(
                            borderColor = Color.Gray, borderWidth = 3.dp
                        ),
                        onClick = { onClick(COMPLICATION_5_ID) },
                    ) {}
                }
            }
            if (page == 3) {
                InfoScreen(navController)
            }
        }
    }
}

@WearPreviewLargeRound
@Composable
private fun OptionPagePreview() {
    OptionPage(labelText = "Color", valueText = "Amber") {}
}

@Composable
private fun OptionPage(valueText: String, labelText: String = "", onStartPicker: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 24.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = labelText, style = MaterialTheme.typography.caption1)
            CompactChip(label = {
                Text(
                    text = valueText,
                    modifier = Modifier.padding(0.dp),
                    style = MaterialTheme.typography.caption2
                )
            },
                modifier = Modifier
                    .padding(0.dp)
                    .height(32.dp),
                colors = ChipDefaults.secondaryChipColors(),
                onClick = { onStartPicker() })

        }
    }
}

@WearPreviewLargeRound
@Composable
private fun ListOptionPickerPreview() {
    val options = (0..9).map { option ->
        UserStyleSetting.ListUserStyleSetting.ListOption(
            UserStyleSetting.Option.Id(option.toString()), Resources.getSystem(), 0, 0, null
        )
    }.toList()
    ListOptionPicker(
        options = options,
        labelProvider = { _ -> { Text("Lorem ipsum") } },
        secondaryLabelProvider = { _ -> { Text("dolor sit amet") } },
        iconProvider = { _ -> { ColorPreviewIcon("#FFFFFF") } },
        onSelected = {})
}

@Composable
private fun ListOptionPicker(
    options: List<UserStyleSetting.ListUserStyleSetting.ListOption>,
    labelProvider: (String) -> (@Composable RowScope.() -> Unit),
    onSelected: (String) -> Unit,
    secondaryLabelProvider: ((String) -> (@Composable RowScope.() -> Unit))? = null,
    iconProvider: ((String) -> (@Composable BoxScope.() -> Unit))? = null,
) {
    val listState = rememberScalingLazyListState()

    Scaffold(
        positionIndicator = {
            PositionIndicator(
                scalingLazyListState = listState, modifier = Modifier
            )
        },
        vignette = {
            Vignette(vignettePosition = VignettePosition.TopAndBottom)
        },
    ) {
        ScalingLazyColumn(
            contentPadding = PaddingValues(top = 40.dp),
            state = listState,
            modifier = Modifier.fillMaxWidth()
        ) {
            options.map {
                item {
                    Chip(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp, 0.dp),
                        onClick = { onSelected(it.id.toString()) },
                        label = labelProvider(it.id.toString()),
                        secondaryLabel = secondaryLabelProvider?.invoke(it.id.toString()),
                        icon = iconProvider?.invoke(it.id.toString()),
                        colors = ChipDefaults.secondaryChipColors()
                    )
                }
            }
        }
    }
}

@WearPreviewLargeRound
@Composable
private fun ColorPickerPreview() {
    val options = WatchFaceUserStyle.getColorOptionList(LocalContext.current)
    ColorPicker(
        options = options,
        onSelected = {})
}

@Composable
private fun ColorPicker(
    options: List<UserStyleSetting.ListUserStyleSetting.ListOption>,
    onSelected: (String) -> Unit
) {
    ListOptionPicker(
        options = options,
        labelProvider = { optionId -> {
            val option = WatchFaceUserStyle.getColorOption(optionId)
            val text = option?.let { stringResource(it.nameId) } ?: "???"
            Text(text)
        } },
        iconProvider = { optionId -> { ColorPreviewIcon(optionId) } },
        onSelected = onSelected)
}

@WearPreviewLargeRound
@Composable
private fun FontPickerPreview() {
    val options = WatchFaceUserStyle.getFontOptionList(LocalContext.current)
    FontPicker(
        options = options,
        onSelected = {})
}

@Composable
private fun FontPicker(
    options: List<UserStyleSetting.ListUserStyleSetting.ListOption>,
    onSelected: (String) -> Unit,
) {
    ListOptionPicker(
        options = options,
        labelProvider = { optionId -> {
            val option = WatchFaceUserStyle.getFontOption(optionId)
            val text = option?.let { stringResource(it.nameId) } ?: "???"
            val font = option?.let { FontFamily(LocalContext.current.resources.getFont(option.fontId)) }
            Text(text, fontFamily = font)
        } },
        secondaryLabelProvider = { optionId -> {
            val option = WatchFaceUserStyle.getFontOption(optionId)
            val font = option?.let { FontFamily(LocalContext.current.resources.getFont(option.fontId)) }
            Text("0 1 2 3 4 5 6 7 8 9", fontFamily = font)
        } },
        onSelected = onSelected)
}

@WearPreviewLargeRound
@Composable
private fun InfoScreen(
    navController: NavHostController = rememberSwipeDismissableNavController(),
) {
    val listState = rememberScalingLazyListState()

    Scaffold(
        modifier = Modifier.background(MaterialTheme.colors.background),
        positionIndicator = {
            PositionIndicator(
                scalingLazyListState = listState, modifier = Modifier
            )
        },
        vignette = {
            Vignette(vignettePosition = VignettePosition.TopAndBottom)
        },
    ) {
        ScalingLazyColumn(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
            autoCentering = AutoCenteringParams(1)
        ) {
            item {
                Column(
                    modifier = Modifier.padding(bottom = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        stringResource(R.string.app_name),
                        style = MaterialTheme.typography.caption1
                    )
                    Text(BuildConfig.VERSION_NAME, style = MaterialTheme.typography.caption2)
                }
            }
            item {
                Spacer(modifier = Modifier.height(10.dp))
            }
            item {
                Text(
                    stringResource(R.string.license_info_title),
                    style = MaterialTheme.typography.caption1
                )
            }
            item {
                val context = LocalContext.current
                Chip(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp, 0.dp),
                    onClick = {
                        navController.navigate("info/licenses/self")
                    },
                    label = {
                        Text(stringResource(R.string.this_watch_face))
                    },
                    colors = ChipDefaults.secondaryChipColors()
                )
            }
            item {
                Text(
                    stringResource(R.string.license_info_tp_subtitle),
                    style = MaterialTheme.typography.caption2
                )
            }
            item {
                Chip(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp, 0.dp),
                    onClick = {
                        navController.navigate("info/licenses/artwork")
                    },
                    label = {
                        Text(stringResource(R.string.license_info_tp_artwork))
                    },
                    colors = ChipDefaults.secondaryChipColors()
                )
            }
            item {
                val context = LocalContext.current
                Chip(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp, 0.dp),
                    onClick = {
                        context.startActivity(Intent(context, OssLicensesMenuActivity::class.java))
                    },
                    label = {
                        Text(stringResource(R.string.license_info_tp_modules))
                    },
                    colors = ChipDefaults.secondaryChipColors()
                )
            }
        }
    }
}

@WearPreviewLargeRound
@Composable
private fun LicenseListScreen(
    navController: NavHostController = rememberSwipeDismissableNavController(),
) {
    val listState = rememberScalingLazyListState()

    Scaffold(
        positionIndicator = {
            PositionIndicator(
                scalingLazyListState = listState, modifier = Modifier
            )
        },
        vignette = {
            Vignette(vignettePosition = VignettePosition.TopAndBottom)
        },
    ) {
        ScalingLazyColumn(
            contentPadding = PaddingValues(top = 40.dp),
            state = listState,
            modifier = Modifier.fillMaxWidth()
        ) {
            TP_LICENSE_INFOS.withIndex().map { (i, info) ->
                item {
                    Chip(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp, 0.dp),
                        onClick = { navController.navigate("info/licenses/artwork/${i}") },
                        label = {
                            Text(stringResource(info.subjectId))
                        },
                        secondaryLabel = {
                            Text(stringResource(info.licenseNameId))
                        },
                        colors = ChipDefaults.secondaryChipColors()
                    )
                }
            }
        }
    }
}

@Composable
private fun ArtworkLicenseTextScreen(@PreviewParameter(LicenseTextScreenParameterProvider::class) i: Int) {
    val info = TP_LICENSE_INFOS[i]
    LicenseTextScreen(info)
}

@WearPreviewLargeRound
@Composable
private fun LicenseTextScreenPreview() {
    LicenseTextScreen(SELF_LICENSE_INFO)
}

@Composable
private fun LicenseTextScreen(licenseInfo: LicenseInfo) {
    val listState = rememberScalingLazyListState()

    Scaffold(
        positionIndicator = {
            PositionIndicator(
                scalingLazyListState = listState, modifier = Modifier
            )
        },
        vignette = {
            Vignette(vignettePosition = VignettePosition.TopAndBottom)
        },
    ) {
        ScalingLazyColumn(
            contentPadding = PaddingValues(top = 40.dp, bottom = 100.dp),
            state = listState,
            modifier = Modifier.fillMaxWidth(),
            anchorType = ScalingLazyListAnchorType.ItemStart,
        ) {
            item {
                ListHeader {
                    Text(stringResource(id = licenseInfo.subjectId))
                }
            }
            item {
                Text(
                    stringResource(id = licenseInfo.licenseTextId),
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        }
    }
}

@Composable
private fun ColorPreviewIcon(rgbaString: String) {
    val color = Color(parseColor(rgbaString))
    Circle(
        color = color, modifier = Modifier.size(ChipDefaults.IconSize)
    )
}

@Composable
private fun Circle(color: Color, modifier: Modifier = Modifier) {
    Box(modifier = modifier
        .clip(CircleShape)
        .background(color))
}

private class LicenseTextScreenParameterProvider : PreviewParameterProvider<Int> {
    override val values: Sequence<Int> = listOf(0).asSequence()
}
