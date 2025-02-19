/*
 * Copyright (c) 2010-2017 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.evolveum.midpoint.model.impl.lens;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.namespace.QName;

import com.evolveum.midpoint.model.api.context.AssignmentPath;
import com.evolveum.midpoint.model.api.context.AssignmentPathSegment;
import com.evolveum.midpoint.schema.util.ObjectTypeUtil;
import com.evolveum.midpoint.util.DebugUtil;
import com.evolveum.midpoint.xml.ns._public.common.common_3.AssignmentPathType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import org.jetbrains.annotations.NotNull;

/**
 * @author semancik
 *
 */
public class AssignmentPathImpl implements AssignmentPath {
	
	private final List<AssignmentPathSegmentImpl> segments = new ArrayList<>();

	public AssignmentPathImpl() {
	}
	
	@Override
	public List<AssignmentPathSegmentImpl> getSegments() {
		return segments;
	}
	
	public void add(AssignmentPathSegmentImpl segment) {
		segments.add(segment);
	}
	
	public void removeLast(AssignmentPathSegmentImpl segment) {
		AssignmentPathSegmentImpl last = last();
		if (last == null) {
			throw new IllegalStateException("Attempt to remove segment from empty path: " + this + "; segment=" + segment);
		} else if (!last.equals(segment)) {
			throw new IllegalStateException("Attempt to remove wrong segment from the end of path: " + this + "; segment=" + segment);
		} else {
			segments.remove(segments.size() - 1);
		}
	}

	@Override
	public AssignmentPathSegmentImpl first() {
		return segments.get(0);
	}
	
	@Override
	public boolean isEmpty() {
		return segments.isEmpty();
	}

	@Override
	public int size() { return segments.size(); }

//	@Override
//	public EvaluationOrder getEvaluationOrder() {
//		if (isEmpty()) {
//			return EvaluationOrderImpl.ZERO;
//		} else {
//			return last().getEvaluationOrder();
//		}
//	}

	@Override
	public AssignmentPathSegmentImpl last() {
		return beforeLast(0);
	}

	@Override
	public AssignmentPathSegmentImpl beforeLast(int n) {
		if (size() <= n) {
			return null;
		} else {
			return segments.get(segments.size()-1-n);
		}
	}
	
	@Override
	public int countTargetOccurrences(ObjectType target) {
		if (target == null) {
			return 0;
		}
		int count = 0;
		for (AssignmentPathSegment segment: segments) {
			ObjectType segmentTarget = segment.getTarget();
			if (segmentTarget != null) {
				if (segmentTarget.getOid() != null && target.getOid() != null && segmentTarget.getOid().equals(target.getOid())
						|| segmentTarget.getOid() == null && target.getOid() == null && segmentTarget.equals(target)) {
					count++;
				}
			}
		}
		return count;
	}

	@NotNull
	@Override
	public List<ObjectType> getFirstOrderChain() {
		return segments.stream()
				.filter(seg -> seg.isMatchingOrder() && seg.getTarget() != null)
				.map(seg -> seg.getTarget())
				.collect(Collectors.toList());
	}

	/**
	 * Shallow clone.
	 */
	public AssignmentPathImpl clone() {
		AssignmentPathImpl clone = new AssignmentPathImpl();
		clone.segments.addAll(this.segments);
		return clone;
	}

	@Override
	public String toString() {
		return "AssignmentPath(" + segments + ")";
	}

	@Override
	public String debugDump() {
		return debugDump(0);
	}

	@Override
	public String debugDump(int indent) {
		StringBuilder sb = new StringBuilder();
		DebugUtil.debugDumpLabel(sb, "AssignmentPath", indent);
		if (segments.isEmpty()) {
			sb.append(" (empty)");
		} else {
			sb.append(" (").append(segments.size()).append(")");
			if (DebugUtil.isDetailedDebugDump()) {
				sb.append("\n");
				DebugUtil.debugDump(sb, segments, indent + 1, false);
			} else {
				for (AssignmentPathSegmentImpl segment: segments) {
					sb.append("\n");
					DebugUtil.indentDebugDump(sb, indent + 1);
					segment.shortDump(sb);
				}
			}
		}
		return sb.toString();
	}
	
	@Override
	public void shortDump(StringBuilder sb) {
		ObjectType previousTarget = null;
		for (AssignmentPathSegmentImpl segment: segments) {
			if (previousTarget == null) {
				sb.append(segment.getSource()).append(" ");
			}
//			sb.append("(");
//			segment.getEvaluationOrder().shortDump(sb);
//			sb.append("): ");
			ObjectType target = segment.getTarget();
			QName relation = segment.getRelation();
			if (target != null) {
				sb.append("--");
				if (segment.isAssignment()) {
					sb.append("a");
				} else {
					sb.append("i");
				}
				sb.append("[");
				if (relation != null) {
					sb.append(relation.getLocalPart());
				}
				sb.append("]--> ");
				if (target != null) {
					sb.append(target);
				}
			} else {
				if (segment.isAssignment()) {
					sb.append("a");
				} else {
					sb.append("i");
				}
				sb.append("(no target)");
			}
			previousTarget = target;
			sb.append(" ");
		}
	}

	@Override
	public AssignmentPathType toAssignmentPathType() {
		AssignmentPathType rv = new AssignmentPathType();
		segments.forEach(seg -> rv.getSegment().add(seg.toAssignmentPathSegmentType()));
		return rv;
	}
}
