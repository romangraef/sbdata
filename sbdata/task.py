import dataclasses
import os
import sys
import typing

import questionary

_T = typing.TypeVar('_T')


class Arguments:

    def __init__(self, args: list[str]):
        self.prog = args[0]
        self.args: typing.Dict[str, str] = {}
        self.flags: list[str] = []
        self.no_prompt = os.environ.get('PROMPT') == 'NO_PROMPT'
        self.task: typing.Optional[str]
        self.task = None
        last_arg = None
        for arg in args:
            if last_arg is None:
                if arg.startswith('--'):
                    last_arg = arg[2:]
                elif arg.startswith('-'):
                    self.flags.append(arg[1:])
                elif arg.startswith(':'):
                    self.task = arg[1:]
                else:
                    print("Unknown arg: " + arg)
            else:
                self.args[last_arg] = arg
                last_arg = None

    def get_value(self, label: str, value: _T, question: questionary.Question) -> _T:
        if value is None:
            if self.no_prompt:
                print('No argument present for ' + label)
                sys.exit(1)
            return question.ask()
        return value

    def get_arg(self, label: str, arg_name: str, mapper: typing.Callable[[str], _T]) -> _T:
        return mapper(self.get_value(label, self.args.get(arg_name), questionary.text('Missing ' + label)))

    def has_flag(self, param: str) -> bool:
        return param in self.flags


@dataclasses.dataclass
class Task:
    label: str
    name: str
    run: typing.Callable


tasks = {}

TASK_TYPE = typing.Callable[[Arguments], None]


def register_task(label: str) -> typing.Callable[[TASK_TYPE], TASK_TYPE]:
    def d(func: TASK_TYPE) -> TASK_TYPE:
        tasks[func.__name__] = Task(label, func.__name__, func)
        return func

    return d
