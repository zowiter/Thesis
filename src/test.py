def frange(x, y, jump):
    while x < y:
        yield x
        x += jump

for i in frange(1.0, 4.0, 0.5):
    print(i)
    if i == 2.5:
        break