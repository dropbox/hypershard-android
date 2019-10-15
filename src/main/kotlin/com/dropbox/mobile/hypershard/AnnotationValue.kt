package com.dropbox.mobile.hypershard

sealed class AnnotationValue {
    class Present(val annotationName: String) : AnnotationValue()
    class Empty : AnnotationValue()
}
