#imports
import subprocess
import csv
from matplotlib import pyplot as plt
import numpy as np

#Configuration of the experiment
seed_c = 1234
seed_s = 4321

num_c = 2
num_s = 100

operant_id = 1
initial_solution_id = 1

alg_m = "h"

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

#Search Alianning parameters
k = 10
l = 0.01
i = 3000
s = 30
program.append(str(k))
program.append(str(l))
program.append(str(i))
program.append(str(s))
program.append(alg_m)

pond = 145000#35000
program.append(str(pond))

#numc nums seedc seeds opid genid

seeds_s = [1313, 1122, 2233, 3344, 4455, 5566, 6677, 7788, 8899, 9900]
seeds_c = [1100, 2211, 3322, 4433, 5544, 6655, 7766, 8877, 9988, 3141]

listaHeurist = [[],[],[],[],[],[],[],[],[],[],[],[],[],[],[],[],[],[],[],[]]
listaCoste = [[],[],[],[],[],[],[],[],[],[],[],[],[],[],[],[],[],[],[],[]]
listaDatos = [[],[],[],[],[],[],[],[],[],[],[],[],[],[],[],[],[],[],[],[]]
listaMilis = [[],[],[],[],[],[],[],[],[],[],[],[],[],[],[],[],[],[],[],[]]
listaExpanded= [[],[],[],[],[],[],[],[],[],[],[],[],[],[],[],[],[],[],[],[]]
listaUnusedCenters = [[],[],[],[],[],[],[],[],[],[],[],[],[],[],[],[],[],[],[],[]]

for rep in range(10):
    print 'r',rep
    program[5] = str(seeds_c[rep])
    program[6] = str(seeds_s[rep])
    for ite in range(20):
        print ite
        #program[3] = str(2*(ite+1))
        program[14] = str(pond + 5000*ite)
        output = subprocess.Popen(program, stdout=subprocess.PIPE).communicate()[0]
        #file = open("./experiment2/"+ str(rep) + "-"+ str(ite) +".out","w")
        #file.write(output)
        #file.close()
        #print output
        reader = csv.reader(output.splitlines())
        listaCsv = list(reader)
        listaUnusedCenters[ite].append(eval(listaCsv[-3][3]))
        listaHeurist[ite].append(eval(listaCsv[-3][2]))
        listaCoste[ite].append(eval(listaCsv[-3][1]))
        listaDatos[ite].append(eval(listaCsv[-3][0]))
        listaMilis[ite].append(eval(listaCsv[-2][0]))
        listaExpanded[ite].append(eval(listaCsv[-1][0]))

plt.autoscale()
plt.boxplot(listaCoste)
plt.suptitle('Cost')
plt.xticks([i for i in range(1,21)],[str(i)+'k' for i in range(145,145+(5*20)+1,5)], rotation='vertical')
plt.xlabel('Ponderacion datos')
plt.ylabel('Coste de la red')
plt.savefig('./experiment7/costeHill.png',bbox_inches='tight')
plt.clf()

plt.autoscale()
plt.boxplot(listaDatos)
plt.suptitle('Datos')
plt.xticks([i for i in range(1,21)],[str(i)+'k' for i in range(145,145+(5*20)+1,5)], rotation='vertical')
plt.xlabel('Ponderacion datos')
plt.ylabel('Datos recibidos')
plt.savefig('./experiment7/datosHill.png',bbox_inches='tight')
plt.clf()

plt.boxplot(listaMilis)
plt.suptitle('Tiempo')
plt.xticks([i for i in range(1,21)],[str(i)+'k' for i in range(145,145+(5*20)+1,5)], rotation='vertical')
plt.xlabel('Ponderacion datos')
plt.ylabel('ms')
plt.savefig('./experiment7/tiempoHill.png',bbox_inches='tight')
plt.clf()
