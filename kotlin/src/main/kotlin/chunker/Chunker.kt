package chunker

class Chunker(
    private val chunkSize: Int = 250,
    private val chunkOverlap: Int = 35,
    private val separators: List<String> = listOf("\n\n", "\n", " ", "")
) {
    
    fun split(text: String): List<String> {
        return splitText(text, separators)
    }
    
    private fun splitText(text: String, separators: List<String>): List<String> {
        val finalChunks = mutableListOf<String>()
        
        val separator = separators.firstOrNull { it in text } ?: separators.last()
        
        val splits = if (separator.isEmpty()) {
            text.toCharArray().map { it.toString() }
        } else {
            text.split(separator).filter { it.isNotEmpty() }
        }
        
        val goodSplits = mutableListOf<String>()
        val remainingSeparators = if (separators.size > 1) separators.drop(1) else separators
        
        for (split in splits) {
            if (split.length < chunkSize) {
                goodSplits.add(split)
            } else {
                if (goodSplits.isNotEmpty()) {
                    val mergedSplits = mergeSplits(goodSplits, separator)
                    finalChunks.addAll(mergedSplits)
                    goodSplits.clear()
                }
                
                val otherInfo = splitText(split, remainingSeparators)
                finalChunks.addAll(otherInfo)
            }
        }
        
        if (goodSplits.isNotEmpty()) {
            val mergedSplits = mergeSplits(goodSplits, separator)
            finalChunks.addAll(mergedSplits)
        }
        
        return finalChunks
    }
    
    private fun mergeSplits(splits: List<String>, separator: String): List<String> {
        val docs = mutableListOf<String>()
        val currentDoc = mutableListOf<String>()
        var total = 0
        
        for (split in splits) {
            val len = split.length + if (currentDoc.isNotEmpty()) separator.length else 0
            
            if (total + len > chunkSize && currentDoc.isNotEmpty()) {
                val doc = currentDoc.joinToString(separator)
                if (doc.isNotEmpty()) {
                    docs.add(doc)
                }
                
                while (total > chunkOverlap || (total + len > chunkSize && total > 0)) {
                    if (currentDoc.isEmpty()) break
                    val removed = currentDoc.removeAt(0)
                    total -= removed.length + if (currentDoc.isNotEmpty()) separator.length else 0
                }
            }
            
            currentDoc.add(split)
            total += len
        }
        
        val doc = currentDoc.joinToString(separator)
        if (doc.isNotEmpty()) {
            docs.add(doc)
        }
        
        return docs
    }
}