#imports
import subprocess
import csv
from matplotlib import pyplot as plt
import numpy as np

#Configuration of the experiment
seed_c = 1234
seed_s = 4321

num_c = 5
num_s = 100

operant_id = 1
initial_solution_id = 1

#Search Alianning parameters
k = 10
l = 0.01
i = 3000
s = 30

program = []

program.append("java")
program.append("-jar")
program.append("CodiPractica/out/artifacts/CodiPractica_jar/CodiPractica.jar")

program.append(str(num_c))
program.append(str(num_s))
program.append(str(seed_c))
program.append(str(seed_s))
program.append(str(operant_id))
program.append(str(initial_solution_id))

program.append(str(k))
program.append(str(l))
program.append(str(i))
program.append(str(s))

program.append('h')

pond = 35000
program.append(str(pond))

seeds_s = [1313, 1122, 2233, 3344, 4455, 5566, 6677, 7788, 8899, 9900]
seeds_c = [1100, 2211, 3322, 4433, 5544, 6655, 7766, 8877, 9988, 3141]

listaUnusedCenters = [[],[],[],[],[],[],[],[],[],[]]

for rep in range(5):
    print 'rep',rep
    program[5] = str(seeds_c[rep])
    program[6] = str(seeds_s[rep])
    for ite in range(10):
        print ite
        program[3]=str(num_c+2*ite)
        output = subprocess.Popen(program, stdout=subprocess.PIPE).communicate()[0]
        reader = csv.reader(output.splitlines())
        listaCsv = list(reader)
        listaUnusedCenters[ite].append(eval(listaCsv[-3][3]))

plt.autoscale()
plt.boxplot(listaUnusedCenters)
plt.suptitle('Centros no usados')
plt.xlabel('#Centros en la red')
plt.xticks([1,2,3,4,5,6,7,8,9,10],[str(i) for i in range(5,200)])
plt.ylabel('#Centros inutilizados')
plt.savefig('./experiment5/centrosInutilizadosDifProporcio.png',bbox_inches='tight')
plt.clf()
