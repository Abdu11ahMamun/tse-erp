// src/components/accounting/CoaTree.tsx

import React, { useMemo } from 'react';
import { ChevronRight, ChevronDown, Circle } from 'lucide-react';
import type { AfmCoa, CoaTreeNode } from '../../types/coa.types';
import { useCoaStore } from '../../store/coaStore';

// ─── Build tree from flat list ───────────────────────────────────────────────

function buildTree(flat: AfmCoa[]): CoaTreeNode[] {
  const map = new Map<number, CoaTreeNode>();

  flat.forEach((item) => {
    map.set(item.id, { ...item, children: [], level: 0 });
  });

  const roots: CoaTreeNode[] = [];

  map.forEach((node) => {
    if (node.parentAccountHeadId === null) {
      node.level = 1;
      roots.push(node);
    } else {
      const parent = map.get(node.parentAccountHeadId);
      if (parent) {
        node.level = parent.level + 1;
        parent.children.push(node);
      } else {
        // Parent not found — treat as root
        node.level = 1;
        roots.push(node);
      }
    }
  });

  return roots;
}

// ─── Single tree node ─────────────────────────────────────────────────────────

interface TreeNodeProps {
  node: CoaTreeNode;
}

function TreeNode({ node }: TreeNodeProps) {
  const { selectedCoa, setSelectedCoa, setFormMode, expandedIds, toggleExpanded } = useCoaStore();

  const isExpanded = expandedIds.has(node.id);
  const isSelected = selectedCoa?.id === node.id;
  const hasChildren = node.children.length > 0;

  const handleClick = () => {
    setSelectedCoa(node);
    setFormMode('edit');
  };

  const handleToggle = (e: React.MouseEvent) => {
    e.stopPropagation();
    toggleExpanded(node.id);
  };

  return (
    <div>
      <div
        className={`flex items-center gap-1 py-0.5 px-2 rounded cursor-pointer group
          ${isSelected ? 'bg-blue-50 text-blue-700' : 'hover:bg-gray-50'}`}
        style={{ paddingLeft: `${(node.level - 1) * 20 + 8}px` }}
        onClick={handleClick}
      >
        {/* Expand/collapse toggle */}
        <span
          className="w-4 h-4 flex items-center justify-center text-gray-400 flex-shrink-0"
          onClick={hasChildren ? handleToggle : undefined}
        >
          {hasChildren ? (
            isExpanded ? (
              <ChevronDown size={13} />
            ) : (
              <ChevronRight size={13} />
            )
          ) : null}
        </span>

        {/* Bullet dot */}
        <Circle
          size={7}
          className={`flex-shrink-0 ${isSelected ? 'text-blue-600 fill-blue-600' : 'text-gray-500 fill-gray-500'}`}
        />

        {/* Label */}
        <span className={`text-sm ml-1 select-none ${isSelected ? 'font-semibold' : 'font-normal'}`}>
          {node.accountCode}-{node.accountHead}
          <span className="text-gray-400 ml-1">({node.level})</span>
        </span>
      </div>

      {/* Children */}
      {hasChildren && isExpanded && (
        <div>
          {node.children.map((child) => (
            <TreeNode key={child.id} node={child} />
          ))}
        </div>
      )}
    </div>
  );
}

// ─── Main CoaTree ─────────────────────────────────────────────────────────────

interface CoaTreeProps {
  data: AfmCoa[];
  isLoading: boolean;
}

export default function CoaTree({ data, isLoading }: CoaTreeProps) {
  const tree = useMemo(() => buildTree(data), [data]);
  const { setExpandedIds, expandedIds } = useCoaStore();

  const expandAll = () => {
    const allIds = new Set(data.map((d) => d.id));
    setExpandedIds(allIds);
  };

  const collapseAll = () => {
    setExpandedIds(new Set());
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-40 text-gray-400 text-sm">
        Loading...
      </div>
    );
  }

  return (
    <div className="h-full flex flex-col">
      {/* Controls */}
      <div className="flex gap-2 mb-2 px-2">
        <button
          onClick={expandAll}
          className="text-xs text-blue-600 hover:underline"
        >
          Expand All
        </button>
        <span className="text-gray-300">|</span>
        <button
          onClick={collapseAll}
          className="text-xs text-blue-600 hover:underline"
        >
          Collapse All
        </button>
      </div>

      {/* Tree */}
      <div className="overflow-y-auto flex-1">
        {tree.map((root) => (
          <TreeNode key={root.id} node={root} />
        ))}
      </div>
    </div>
  );
}
