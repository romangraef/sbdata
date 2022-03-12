import dataclasses
import json
import sys
from typing import Any

import questionary

from sbdata.repo import Item
from sbdata.task import Arguments, tasks


class ObjectEncoder(json.JSONEncoder):

    def default(self, o: Any) -> Any:
        if isinstance(o, Item):
            return o.internalname
        if dataclasses.is_dataclass(o):
            return o.__dict__
        return super().default(o)


def main():
    args = Arguments(sys.argv)
    task = args.get_value(
        'Task', tasks.get(args.task),
        questionary.select('Which task do you want to execute?', choices=[
            questionary.Choice(task.label, task) for task in tasks.values()
        ]))
    print("Selected task: " + task.label)
    data = task.run(args)
    if args.has_flag('json'):
        print(json.dumps(data, cls=ObjectEncoder))


if __name__ == '__main__':
    main()
