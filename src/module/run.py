import os
import csv
from booksim import Booksim
from gpnocsim import Gpnocsim
from topaz import Topaz
from helper import Helper

os.getcwd()
os.chdir('../')
PATH = os.getcwd() +'\\'
PATH = PATH.replace('\\', '\\\\')

if os.path.exists(PATH + "result\\result.csv"):
    pass
else:
    data = [['model', 'topology', 'traffic pattern', 'size', 'network throughput']]
    with open(PATH + 'result\\result.csv', 'w', newline='') as f:
        writer = csv.writer(f)
        for row in data:
            writer.writerow(row)


err = 1

while err == 1:
    choice = str(input('Choose model. Write "booksim", "gpnocsim" or "topaz"\n').strip().lower())
    if  choice == 'booksim': #BOOKSIM
        Helper = Helper()
        topology, size, num_vcs, buf_size, routing_func = Helper.booksim_params()
        BooksimModel = Booksim(topology, size, num_vcs, buf_size, routing_func)
        BooksimModel.simulation(PATH)
        err = 0
    elif choice == 'gpnocsim': #GPNOCSIM
        Helper = Helper()
        topology, size, num_vsc = Helper.gpnocsim_params()
        GpnocsimModel = Gpnocsim(topology, size, num_vsc)
        GpnocsimModel.simulation(PATH)
        err = 0
    elif choice == 'topaz': #TOPAZ
        Helper = Helper()
        topology, size = Helper.topaz_params()
        TopazModel = Topaz(topology, size)
        TopazModel.simulation(PATH)
        err = 0
    else:
        print("Error: Write 'booksim', 'gpnocsim' or 'topaz'")
        err = 1