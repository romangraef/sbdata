import functools
from typing import TypeVar, Generator, ParamSpec, Callable

_Param = ParamSpec('_Param')
_RetType = TypeVar('_RetType')


def no_generator(func: Callable[_Param, Generator[_RetType, None, None]]) -> Callable[_Param, list[_RetType]]:
    @functools.wraps(func)
    def wrapper(*args, **kwargs) -> list[_RetType]:
        return list(func(*args, **kwargs))

    return wrapper
