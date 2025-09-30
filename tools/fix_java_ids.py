import json
import os

BASE_DIR = os.path.dirname(os.path.dirname(__file__))
FILE_PATH = os.path.join(BASE_DIR, 'ReactJSInterviewQuestionsAndAnswers', 'java-questions.json')


def fix_ids(file_path: str) -> int:
    with open(file_path, 'r', encoding='utf-8') as f:
        data = json.load(f)
    if not isinstance(data, list):
        raise ValueError('Expected a list of question objects')

    for idx, item in enumerate(data, start=1):
        if isinstance(item, dict):
            item['id'] = idx
        else:
            raise ValueError(f'Unexpected item type at position {idx}: {type(item)}')

    with open(file_path, 'w', encoding='utf-8') as f:
        json.dump(data, f, ensure_ascii=False, indent=2)

    return len(data)


if __name__ == '__main__':
    try:
        total = fix_ids(FILE_PATH)
        print(f'Fixed IDs for {total} questions in {FILE_PATH}.')
    except Exception as e:
        print(f'Error fixing IDs: {e}')
