package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun TindaSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "Search name, SKU, QR, category...",
    modifier: Modifier = Modifier,
    onFilterClick: (() -> Unit)? = null,
    isFilterActive: Boolean = false
) {
    val colors = LocalTindaColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(100.dp))
            .background(colors.cardCream)
            .border(
                1.dp,
                if (isFilterActive) colors.forest else colors.cardBorder,
                RoundedCornerShape(100.dp)
            )
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search",
            tint = if (query.isNotBlank()) colors.forest else colors.muted,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart
        ) {
            if (query.isEmpty()) {
                Text(
                    text = placeholder,
                    color = colors.muted,
                    fontSize = 14.sp,
                    fontFamily = DmSansFontFamily
                )
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                textStyle = TextStyle(
                    color = colors.ink,
                    fontSize = 14.sp,
                    fontFamily = DmSansFontFamily
                ),
                cursorBrush = SolidColor(colors.forest),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (query.isNotEmpty()) {
            IconButton(
                onClick = { onQueryChange("") },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Clear search",
                    tint = colors.muted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        if (onFilterClick != null) {
            Spacer(modifier = Modifier.width(4.dp))
            IconButton(
                onClick = onFilterClick,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(if (isFilterActive) colors.forest else colors.sageSoft)
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Filter",
                    tint = if (isFilterActive) Color.White else colors.forest,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
