import csv
model = 'newxim'
topology = 'mesh'
size = '4 4'
PATH = 'C:\\Users\\eveli\\Desktop\\Diploma\\src\\'
with open("C:\\Users\\eveli\\Desktop\\Diploma\\src\\result\\result_newxim.txt", "r") as f:
    lines = list(f)
    f.close()
line = str(lines[5])
line = line.replace("% ", "")
line = line.replace("\n", "")
line = line.replace(":", "")
line = str(line).split()
result = line[3]
data = [['model', 'topology', 'size', 'network throughput'],
        [model, topology, size, result ]]

with open('C:\\Users\\eveli\\Desktop\\Diploma\\src\\result\\result.csv', 'w', newline='') as f:
    writer = csv.writer(f)
    for row in data:
        writer.writerow(row)

data = [[model, topology, size, result ]]
with open('C:\\Users\\eveli\\Desktop\\Diploma\\src\\result\\result.csv', 'a', newline='') as f:
    writer = csv.writer(f)
    for row in data:
        writer.writerow(row)