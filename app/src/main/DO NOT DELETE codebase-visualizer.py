import os
import sys
import argparse
import fnmatch
from datetime import datetime

class CodebaseVisualizer:
    def __init__(self, root_path, ignore_patterns=None, max_depth=None):
        self.root_path = os.path.abspath(root_path)
        self.ignore_patterns = ignore_patterns or [
            '*.pyc', '*.pyo', '*.pyd', 
            '.git', '.svn', '.hg', 
            '__pycache__', '.pytest_cache', 
            'node_modules', 'venv', '.venv', 
            '.idea', '.vscode', '.env', 
            '*.iml', 'build', '.gradle', 
            '*.log', '*.bak'
        ]
        self.max_depth = max_depth

    def _should_ignore(self, name):
        return any(fnmatch.fnmatch(name, pattern) for pattern in self.ignore_patterns)

    def visualize(self, output_format='tree'):
        if output_format == 'tree':
            return self._generate_tree_view()
        elif output_format == 'list':
            return self._generate_list_view()
        else:
            raise ValueError("Invalid output format. Choose 'tree' or 'list'.")

    def _generate_tree_view(self):
        def walk_directory(directory, prefix='', is_last_entry=True, depth=0):
            if self.max_depth is not None and depth > self.max_depth:
                return []
            
            tree_view = []
            try:
                items = [
                    item for item in sorted(os.listdir(directory)) 
                    if not self._should_ignore(item)
                ]
                
                for i, item in enumerate(items):
                    is_last = (i == len(items) - 1)
                    full_path = os.path.join(directory, item)
                    
                    current_prefix = prefix + ('└── ' if is_last_entry and is_last else '├── ')
                    next_prefix = prefix + ('    ' if is_last_entry and is_last else '│   ')
                    
                    tree_view.append(current_prefix + item + ('\\' if os.path.isdir(full_path) else ''))
                    
                    if os.path.isdir(full_path):
                        tree_view.extend(
                            walk_directory(
                                full_path, 
                                next_prefix, 
                                is_last, 
                                depth + 1
                            )
                        )
            
            except PermissionError:
                tree_view.append(prefix + '└── [Permission Denied]')
            
            return tree_view

        full_tree = [os.path.basename(self.root_path) + '\\'] + walk_directory(self.root_path)
        return '\n'.join(full_tree)

    def _generate_list_view(self):
        list_view = []
        for root, dirs, files in os.walk(self.root_path):
            dirs[:] = [d for d in dirs if not self._should_ignore(d)]
            
            if any(self._should_ignore(os.path.basename(root))):
                continue
            
            rel_path = os.path.relpath(root, self.root_path)
            
            if rel_path != '.':
                list_view.append(rel_path + '\\')
            
            for file in files:
                if not self._should_ignore(file):
                    list_view.append(os.path.join(rel_path, file))
        
        return '\n'.join(sorted(list_view))

def main():
    parser = argparse.ArgumentParser(description='Visualize codebase hierarchy')
    parser.add_argument('path', nargs='?', default='.', 
                        help='Path to the root of the codebase (default: current directory)')
    parser.add_argument('-d', '--depth', type=int, 
                        help='Maximum depth of directory traversal')
    parser.add_argument('-f', '--format', choices=['tree', 'list'], 
                        default='tree', 
                        help='Output format (default: tree)')
    parser.add_argument('-i', '--ignore', nargs='+', 
                        help='Additional patterns to ignore')
    
    args = parser.parse_args()
    
    try:
        if sys.platform == 'win32':
            sys.stdout.reconfigure(encoding='utf-8')
        
        visualizer = CodebaseVisualizer(
            args.path, 
            ignore_patterns=args.ignore, 
            max_depth=args.depth
        )
        
        # Visualize the codebase
        visualization = visualizer.visualize(args.format)
        
        # Generate filename with timestamp
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        desktop_path = os.path.join(os.path.expanduser('~'), 'Desktop')
        output_filename = f'codebase_structure_{timestamp}.txt'
        output_path = os.path.join(desktop_path, output_filename)
        
        # Save to desktop
        with open(output_path, 'w', encoding='utf-8') as f:
            f.write(visualization)
        
        # Print confirmation message
        print(f"Codebase structure saved to: {output_path}")
        
        # Also print to console
        print("\nCodebase Structure:")
        print(visualization)
    
    except Exception as e:
        print(f"Error: {e}", file=sys.stderr)
        sys.exit(1)

if __name__ == '__main__':
    main()
