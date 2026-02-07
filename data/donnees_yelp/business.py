import json
import ast

def explore_structure(data, schema, prefix=""):
    if isinstance(data, dict):
        for key, value in data.items():
            full_key = f"{prefix}.{key}" if prefix else key
            
            if isinstance(value, str) and value.startswith('{'):
                try:
                    value = ast.literal_eval(value)
                except:
                    pass

            if isinstance(value, dict):
                explore_structure(value, schema, full_key)
            else:
                schema[full_key] = type(value).__name__

def get_full_schema(file_path):
    full_schema = {}
    with open(file_path, 'r', encoding='utf-8') as f:
        for i, line in enumerate(f):
            record = json.loads(line)
            explore_structure(record, full_schema)
    return full_schema

path = "yelp_academic_dataset_business.json"
final_mapping = get_full_schema(path)

print("\nSTRUCTURE")
for key in sorted(final_mapping.keys()):
    print(f"{key} ({final_mapping[key]})")