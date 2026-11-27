package com.example.model

data class DesignTemplate(
    val id: String,
    val title: String,
    val titleBn: String,
    val category: String,
    val sizeLabel: String,
    val subtitle: String,
    val documentBuilder: () -> CanvasDocument
)

object TemplateCatalog {
    val templates: List<DesignTemplate> =
        TemplateCatalogPart1.templates + TemplateCatalogPart2.templates
}
