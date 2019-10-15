package com.dropbox.mobile.hypershard

/**
 * Annotation name for Kotlin or Java classes
 *
 */
sealed class ClassAnnotationValue {
    class Present(val annotationName: String) : ClassAnnotationValue()
    class Empty : ClassAnnotationValue()
}
