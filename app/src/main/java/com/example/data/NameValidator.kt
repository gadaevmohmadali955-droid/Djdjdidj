package com.example.data

object NameValidator {

    // Comprehensive collection of real human names (Slavic, Russian, Caucasian, International)
    private val REAL_NAMES = hashSetOf(
        // Male names (Russian / Slavic / Caucasian / Central Asian / International)
        "мохьмад", "мохмад", "магомед", "мухаммад", "мухаммед", "адам", "али", "ислам",
        "амир", "мансур", "байсангур", "хамзат", "асламбек", "шамиль", "руслан", "тимур",
        "александр", "алексей", "андрей", "антон", "артем", "артём", "артур", "богдан",
        "борис", "вадим", "валентин", "валерий", "василий", "виктор", "виталий", "владимир",
        "владислав", "вячеслав", "георгий", "глеб", "григорий", "даниил", "данил", "данила",
        "денис", "дмитрий", "евгений", "егор", "иван", "игорь", "илья", "кирилл",
        "константин", "лев", "леонид", "максим", "марк", "матвей", "михаил", "никита",
        "николай", "олег", "павел", "петр", "пётр", "роберт", "родио́н", "родион", "роман",
        "ростислав", "семен", "семён", "сергей", "станислав", "степан", "тарас", "тимофей",
        "федор", "фёдор", "филипп", "эдуард", "юрий", "ярослав",
        // Female names
        "анна", "алина", "алиса", "алена", "алёна", "анастасия", "ангелина", "арина",
        "валентина", "валерия", "варвара", "василиса", "вера", "вероника", "виктория",
        "галина", "дарья", "диана", "ева", "евгения", "екатерина", "елена", "елизавета",
        "жанна", "злата", "инна", "ирина", "карина", "кира", "кристина", "ксения",
        "лада", "лариса", "любовь", "людмила", "майя", "маргарита", "марина", "мария",
        "милана", "мирослава", "надежда", "наталья", "наталия", "нелли", "ника", "нина",
        "оксана", "олеся", "ольга", "полина", "рада", "рената", "светлана", "снежана",
        "софия", "софья", "таисия", "тамара", "татьяна", "ульяна", "юлия", "яна", "ярослава",
        "малика", "амина", "хеда", "петина", "марха", "зарема", "луиза", "милана", "камилла",
        // Common Latin names
        "alex", "alexander", "adam", "andrew", "anthony", "arthur", "benjamin", "charles",
        "daniel", "david", "edward", "eric", "george", "jack", "james", "john", "joseph",
        "leo", "lucas", "mark", "matthew", "michael", "nicholas", "noah", "oliver", "paul",
        "peter", "richard", "robert", "samuel", "thomas", "william", "alice", "anna",
        "charlotte", "elizabeth", "emily", "emma", "grace", "hanna", "julia", "lily",
        "lucy", "maria", "mary", "olivia", "sarah", "sophia", "victoria"
    )

    data class ValidationResult(
        val isValid: Boolean,
        val formattedName: String,
        val errorMessage: String? = null
    )

    fun validateRealName(input: String): ValidationResult {
        val trimmed = input.trim()

        if (trimmed.isEmpty()) {
            return ValidationResult(
                isValid = false,
                formattedName = "",
                errorMessage = "Пожалуйста, введите ваше настоящее имя."
            )
        }

        if (trimmed.length < 2) {
            return ValidationResult(
                isValid = false,
                formattedName = "",
                errorMessage = "Имя слишком короткое. Напишите настоящее имя!"
            )
        }

        if (trimmed.length > 25) {
            return ValidationResult(
                isValid = false,
                formattedName = "",
                errorMessage = "Имя слишком длинное. Введите настоящее имя."
            )
        }

        // Must not contain digits or weird symbols
        if (!trimmed.matches(Regex("^[a-zA-Zа-яА-ЯёЁ\\s\\-]+$"))) {
            return ValidationResult(
                isValid = false,
                formattedName = "",
                errorMessage = "Напишите настоящее имя! В имени не может быть цифр и спецсимволов."
            )
        }

        // Split into parts (e.g. compound names like "Анна-Мария")
        val parts = trimmed.split(Regex("[\\s\\-]+")).filter { it.isNotBlank() }
        for (part in parts) {
            if (part.length < 2) {
                return ValidationResult(
                    isValid = false,
                    formattedName = "",
                    errorMessage = "Напишите настоящее имя!"
                )
            }

            // Check for repeated single character like "aaaa", "фффф"
            if (part.toSet().size == 1) {
                return ValidationResult(
                    isValid = false,
                    formattedName = "",
                    errorMessage = "Напишите настоящее имя! Это не похоже на реальное имя человека."
                )
            }

            // Check phonetic balance (must have at least one vowel)
            val vowelsRu = "аеёиоуыэюяaeiouy"
            val hasVowel = part.any { it.lowercaseChar() in vowelsRu }
            if (!hasVowel) {
                return ValidationResult(
                    isValid = false,
                    formattedName = "",
                    errorMessage = "Напишите настоящее имя! В слове нет гласных букв."
                )
            }

            // Reject obvious keyboard mash / spam sequences
            val lower = part.lowercase()
            val keyboardMashes = listOf(
                "asdf", "qwer", "zxcv", "йцук", "фыва", "ячсм", "qwerty", "asdfgh",
                "test", "тест", "admin", "админ", "bot", "бот", "roblox", "роблокс",
                "ыва", "вфы", "джл", "прл"
            )
            if (keyboardMashes.any { lower.contains(it) }) {
                return ValidationResult(
                    isValid = false,
                    formattedName = "",
                    errorMessage = "Напишите настоящее имя! Это случайный набор букв."
                )
            }
        }

        val primaryName = parts[0].lowercase()

        // Check if known in our rich real names registry or passes high-confidence human phonetic criteria
        val isRecognizedName = REAL_NAMES.contains(primaryName) ||
                (primaryName.length in 3..14 && isValidHumanNamePattern(primaryName))

        if (!isRecognizedName) {
            return ValidationResult(
                isValid = false,
                formattedName = "",
                errorMessage = "Сайт не нашел такого имени в реальной жизни. Пожалуйста, напишите ваше настоящее имя!"
            )
        }

        // Capitalize properly: e.g. "мохьмад" -> "Мохьмад"
        val formatted = parts.joinToString(" ") { p ->
            p.lowercase().replaceFirstChar { it.uppercase() }
        }

        return ValidationResult(
            isValid = true,
            formattedName = formatted,
            errorMessage = null
        )
    }

    private fun isValidHumanNamePattern(lower: String): Boolean {
        val vowels = "аеёиоуыэюяaeiouy"
        var consecutiveConsonants = 0
        var maxConsonants = 0
        for (c in lower) {
            if (c !in vowels) {
                consecutiveConsonants++
                if (consecutiveConsonants > maxConsonants) maxConsonants = consecutiveConsonants
            } else {
                consecutiveConsonants = 0
            }
        }
        // If more than 4 consonants in a row, likely keyboard mash (e.g. "dsfgj")
        if (maxConsonants > 4) return false

        // Check character variety (at least 3 distinct letters for names > 3 chars)
        if (lower.length >= 4 && lower.toSet().size < 3) return false

        return true
    }
}
