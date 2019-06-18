/*
 * Copyright (c) 2011-2019 Nexmo Inc
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.nexmo.jwt

import com.nexmo.jwt.utils.remove
import java.util.*

internal class Acl(@Suppress("unused") val paths: Map<String, Path>) {
    class Path(val methods: MutableSet<Scope.Method>)

    internal companion object {
        fun fromScopes(scopes: Set<Scope>): Acl {
            val pathMap = scopes.groupBy({ it.path }, { it.methods })
                .mapValues { Path(it.value.flatten().toMutableSet()) }

            // Copy the map for filtering
            val result = LinkedHashMap(pathMap)

            pathMap.filter { it.key.contains("/**") }.forEach {
                it.value.methods.addAll(pathMap.filter { (k, _) ->
                    k in relevantKeyList(it.key)
                }.values.flatMap { path -> path.methods })

                removeEngulfedKeys(result, it.key)
            }

            return Acl(paths = result)
        }

        private fun relevantKeyList(key: String) = listOf(
            key,
            key.replace("/**", ""),
            key.replace("/**", "/"),
            key.replace("/**", "/*")
        )

        private fun removeEngulfedKeys(map: MutableMap<String, Path>, engulfingKey: String) {
            val noWildCardName = engulfingKey.replace("/**", "")
            val slashName = engulfingKey.replace("/**", "/")
            val singleWildCardName = engulfingKey.replace("/**", "/*")
            map.remove(noWildCardName, slashName, singleWildCardName)
        }
    }
}