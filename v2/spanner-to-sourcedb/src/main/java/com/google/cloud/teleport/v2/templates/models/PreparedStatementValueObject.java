/*
 * Copyright (C) 2024 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.cloud.teleport.v2.templates.models;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class PreparedStatementValueObject<T> {

  public abstract String dataType();

  public abstract T value();

  public static <T> PreparedStatementValueObject<T> create(String dataType, T value) {
    return new AutoValue_PreparedStatementValueObject<>(dataType, value);
  }
}
