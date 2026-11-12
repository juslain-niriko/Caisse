package mg.caisse.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import java.io.File

@Composable
fun ReceiptThumb(path: String?, modifier: Modifier = Modifier) {
    if (path != null) {
        AsyncImage(
            model = File(path),
            contentDescription = "Justificatif",
            contentScale = ContentScale.Crop,
            modifier = modifier
                .size(72.dp)
                .clip(RoundedCornerShape(12.dp))
        )
    }
}
