package com.example.domain.calculators

import kotlin.math.*

enum class AngleMode {
    DEG, RAD
}

class ScientificEvaluator(private val angleMode: AngleMode = AngleMode.DEG) {

    fun evaluate(expression: String): Double {
        val sanitized = expression
            .replace("×", "*")
            .replace("÷", "/")
            .replace("π", "pi")
            .replace(" ", "")

        if (sanitized.isBlank()) {
            throw IllegalArgumentException("Expression vide.")
        }

        val tokens = tokenize(sanitized)
        val parser = Parser(tokens, angleMode)
        return parser.parse()
    }

    private fun tokenize(expr: String): List<String> {
        val tokens = mutableListOf<String>()
        var i = 0
        while (i < expr.length) {
            val c = expr[i]
            when {
                c.isDigit() || c == '.' -> {
                    val sb = StringBuilder()
                    while (i < expr.length && (expr[i].isDigit() || expr[i] == '.')) {
                        sb.append(expr[i])
                        i++
                    }
                    tokens.add(sb.toString())
                }
                c.isLetter() -> {
                    val sb = StringBuilder()
                    while (i < expr.length && expr[i].isLetter()) {
                        sb.append(expr[i])
                        i++
                    }
                    tokens.add(sb.toString().lowercase())
                }
                c in "+-*/^()%" -> {
                    tokens.add(c.toString())
                    i++
                }
                else -> {
                    throw IllegalArgumentException("Caractère non reconnu : '$c'")
                }
            }
        }
        return tokens
    }

    private class Parser(private val tokens: List<String>, private val angleMode: AngleMode) {
        private var pos = 0

        fun parse(): Double {
            val result = parseExpression()
            if (pos < tokens.size) {
                throw IllegalArgumentException("Symbole inattendu en fin d'expression : '${tokens[pos]}'")
            }
            return result
        }

        // Expression = Term (('+' | '-') Term)*
        private fun parseExpression(): Double {
            var value = parseTerm()
            while (pos < tokens.size && (tokens[pos] == "+" || tokens[pos] == "-")) {
                val op = tokens[pos++]
                val nextTerm = parseTerm()
                value = if (op == "+") value + nextTerm else value - nextTerm
            }
            return value
        }

        // Term = Power (('*' | '/' | '%') Power)*
        private fun parseTerm(): Double {
            var value = parsePower()
            while (pos < tokens.size && (tokens[pos] == "*" || tokens[pos] == "/" || tokens[pos] == "%")) {
                val op = tokens[pos++]
                val nextPower = parsePower()
                when (op) {
                    "*" -> value *= nextPower
                    "/" -> {
                        if (nextPower == 0.0) throw ArithmeticException("Division par zéro.")
                        value /= nextPower
                    }
                    "%" -> {
                        if (nextPower == 0.0) throw ArithmeticException("Modulo par zéro.")
                        value %= nextPower
                    }
                }
            }
            return value
        }

        // Power = Factor ('^' Power)? (right-associative)
        private fun parsePower(): Double {
            val base = parseFactor()
            if (pos < tokens.size && tokens[pos] == "^") {
                pos++
                val exponent = parsePower()
                return base.pow(exponent)
            }
            return base
        }

        // Factor = Unary | Number | Constant | '(' Expression ')' | Function '(' Expression ')'
        private fun parseFactor(): Double {
            if (pos >= tokens.size) throw IllegalArgumentException("Fin d'expression inattendue.")

            val token = tokens[pos++]

            // Unary + or -
            if (token == "+") return parseFactor()
            if (token == "-") return -parseFactor()

            // Parentheses
            if (token == "(") {
                val value = parseExpression()
                if (pos >= tokens.size || tokens[pos++] != ")") {
                    throw IllegalArgumentException("Parenthèse fermante ')' manquante.")
                }
                return value
            }

            // Constants
            if (token == "pi") return Math.PI
            if (token == "e") return Math.E

            // Number
            token.toDoubleOrNull()?.let { return it }

            // Functions: sin, cos, tan, asin, acos, atan, sqrt, cbrt, ln, log, abs, exp
            if (pos < tokens.size && tokens[pos] == "(") {
                pos++ // consume '('
                val arg = parseExpression()
                if (pos >= tokens.size || tokens[pos++] != ")") {
                    throw IllegalArgumentException("Parenthèse fermante ')' attendue après la fonction $token.")
                }
                return applyFunction(token, arg)
            }

            throw IllegalArgumentException("Jeton inattendu : '$token'")
        }

        private fun applyFunction(func: String, arg: Double): Double {
            return when (func) {
                "sin" -> {
                    val rad = if (angleMode == AngleMode.DEG) Math.toRadians(arg) else arg
                    sin(rad)
                }
                "cos" -> {
                    val rad = if (angleMode == AngleMode.DEG) Math.toRadians(arg) else arg
                    cos(rad)
                }
                "tan" -> {
                    val rad = if (angleMode == AngleMode.DEG) Math.toRadians(arg) else arg
                    val c = cos(rad)
                    if (abs(c) < 1e-15) throw ArithmeticException("Tangente indéfinie (cosinus nul).")
                    tan(rad)
                }
                "asin" -> {
                    if (arg < -1.0 || arg > 1.0) throw ArithmeticException("Domaine invalide pour asin (doit être entre -1 et 1).")
                    val rad = asin(arg)
                    if (angleMode == AngleMode.DEG) Math.toDegrees(rad) else rad
                }
                "acos" -> {
                    if (arg < -1.0 || arg > 1.0) throw ArithmeticException("Domaine invalide pour acos (doit être entre -1 et 1).")
                    val rad = acos(arg)
                    if (angleMode == AngleMode.DEG) Math.toDegrees(rad) else rad
                }
                "atan" -> {
                    val rad = atan(arg)
                    if (angleMode == AngleMode.DEG) Math.toDegrees(rad) else rad
                }
                "sqrt" -> {
                    if (arg < 0.0) throw ArithmeticException("Racine carrée d'un nombre négatif non supportée.")
                    sqrt(arg)
                }
                "cbrt" -> cbrt(arg)
                "ln" -> {
                    if (arg <= 0.0) throw ArithmeticException("Logarithme naturel défini uniquement pour x > 0.")
                    ln(arg)
                }
                "log", "log10" -> {
                    if (arg <= 0.0) throw ArithmeticException("Logarithme décimal défini uniquement pour x > 0.")
                    log10(arg)
                }
                "abs" -> abs(arg)
                "exp" -> exp(arg)
                else -> throw IllegalArgumentException("Fonction mathématique inconnue : '$func'")
            }
        }
    }
}
