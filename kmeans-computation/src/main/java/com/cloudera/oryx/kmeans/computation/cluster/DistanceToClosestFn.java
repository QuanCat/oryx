/*
 * Copyright (c) 2013, Cloudera, Inc. All Rights Reserved.
 *
 * Cloudera, Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"). You may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * This software is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations under the
 * License.
 */

package com.cloudera.oryx.kmeans.computation.cluster;

import com.cloudera.oryx.computation.common.fn.OryxDoFn;
import com.cloudera.oryx.kmeans.common.Distance;
import com.cloudera.oryx.kmeans.computation.AvroUtils;
import org.apache.commons.math3.linear.RealVector;
import org.apache.crunch.CrunchRuntimeException;
import org.apache.crunch.Emitter;
import org.apache.crunch.Pair;

public final class DistanceToClosestFn<V extends RealVector> extends OryxDoFn<Pair<Integer, V>, Pair<Integer, Pair<V, Double>>> {
  private String indexKey;
  private KSketchIndex index;

  DistanceToClosestFn(String indexKey) {
    this.indexKey = indexKey;
  }

  public DistanceToClosestFn(KSketchIndex index) {
    this.index = index;
  }

  @Override
  public void initialize() {
    super.initialize();
    if (index == null) {
      try {
        index = AvroUtils.readSerialized(indexKey, getConfiguration());
      } catch (Exception e) {
        throw new CrunchRuntimeException(e);
      }
    }
  }

  @Override
  public void process(Pair<Integer, V> in, Emitter<Pair<Integer, Pair<V, Double>>> emitter) {
    Distance d = index.getDistance(in.second(), in.first(), true);
    if (d.getSquaredDistance() > 0.0) {
      emitter.emit(Pair.of(in.first(), Pair.of(in.second(), d.getSquaredDistance())));
    }
  }
}
