/*
 * Created by Injoit on 13.9.2024.
 * Copyright © 2024 Quickblox. All rights reserved.
 *
 */

package com.quickblox.android_ui_kit.domain

enum class Language(val code: String) {
    English("en"),
    Spanish("es"),
    ChineseSimplified("zh-Hans"),
    ChineseTraditional("zh-Hant"),
    French("fr"),
    German("de"),
    Japanese("ja"),
    Korean("ko"),
    Italian("it"),
    Russian("ru"),
    Portuguese("pt"),
    Arabic("ar"),
    Hindi("hi"),
    Turkish("tr"),
    Dutch("nl"),
    Polish("pl"),
    Ukrainian("uk"),
    Albanian("sq"),
    Armenian("hy"),
    Azerbaijani("az"),
    Basque("eu"),
    Belarusian("be"),
    Bengali("bn"),
    Bosnian("bs"),
    Bulgarian("bg"),
    Catalan("ca"),
    Croatian("hr"),
    Czech("cs"),
    Danish("da"),
    Estonian("et"),
    Finnish("fi"),
    Galician("gl"),
    Georgian("ka"),
    Greek("el"),
    Gujarati("gu"),
    Hungarian("hu"),
    Indonesian("id"),
    Irish("ga"),
    Kannada("kn"),
    Kazakh("kk"),
    Latvian("lv"),
    Lithuanian("lt"),
    Macedonian("mk"),
    Malay("ms"),
    Maltese("mt"),
    Mongolian("mn"),
    Nepali("ne"),
    Norwegian("no"),
    Pashto("ps"),
    Persian("fa"),
    Punjabi("pa"),
    Romanian("ro"),
    Sanskrit("sa"),
    Serbian("sr"),
    Sindhi("sd"),
    Sinhala("si"),
    Slovak("sk"),
    Slovenian("sl"),
    Uzbek("uz"),
    Vietnamese("vi"),
    Welsh("cy");

    companion object {
        fun isLanguageSupported(code: String): Boolean {
            return values().any { it.code == code }
        }
    }
}
