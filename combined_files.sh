#!/bin/bash

# Concatenate all files in current directory into markdown format
OUTPUT_FILE="combined_files.md"

# Clear or create output file
> "$OUTPUT_FILE"

# Add initial markdown header
echo "# Combined Files" > "$OUTPUT_FILE"
echo "" >> "$OUTPUT_FILE"
echo "Generated on: $(date)" >> "$OUTPUT_FILE"
echo "" >> "$OUTPUT_FILE"

# Loop through all files (skip the script itself)
for file in $(find pom.xml README.md src start.sh -type f); do
    # Skip directories and the script itself
    if [ -f "$file" ] && [ "$file" != "$(basename "$0")" ]; then
        echo "## File: $file" >> "$OUTPUT_FILE"
        echo "" >> "$OUTPUT_FILE"
        echo "````" >> "$OUTPUT_FILE"
        cat "$file" >> "$OUTPUT_FILE"
        echo "" >> "$OUTPUT_FILE"
        echo "````" >> "$OUTPUT_FILE"
        echo "" >> "$OUTPUT_FILE"
    fi
done

echo "All files concatenated into $OUTPUT_FILE in markdown format"

