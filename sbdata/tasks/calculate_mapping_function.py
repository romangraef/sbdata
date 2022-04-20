import pathlib
import random
import numpy as np
from sklearn.linear_model import LinearRegression

from sbdata.task import register_task, Arguments


@register_task("Calculate Map Coordinate Function")
def calculate_mapping_function(args: Arguments):
    csv = args.get_arg("Coordinate CSV", "coords", pathlib.Path)
    points = [[int(x) for x in y.split(",")] for y in csv.read_text().splitlines()[1:]]
    xs = [(a[0], a[2]) for a in points]
    zs = [(a[1], a[3]) for a in points]
    random.shuffle(xs)
    random.shuffle(zs)
    find_best_function_for("X", xs)
    find_best_function_for("Z", zs)


def find_best_function_for(label: str, l: list[tuple[int, int]]):
    x = np.array([a[0] for a in l]).reshape((-1, 1))
    y = np.array([a[1] for a in l])
    model = LinearRegression()
    model.fit(x, y)
    print(f'------------')
    print(f' {label} Coordinate:')
    print(f"   Score: {model.score(x, y)}")
    print(f"   Slope: {model.coef_[0]}")
    print(f"   Intercept: {model.intercept_}")
    print(f'------------')
