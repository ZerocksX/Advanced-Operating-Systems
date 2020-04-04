import posix_ipc
from multiprocessing import Process
from time import time, sleep
import random

"""
PROBLEM PUŠAČA CIGARETA
„Problem pušača cigareta“ predstavio je Patil kako bi ukazao na problem sinkronizacije korištenjem semafora.
 U ovom radu koristi se isti problem, ali različito rješenje radi ilustracije sinkronizacije u raspodijeljenom okruženju.

U ovom slučaju sustav se sastoji od tri procesa koje predstavljaju pušače te jednog procesa trgovca. 
Svaki od „pušača“ ima kod sebe u neograničenoj količini jedan od tri sastojka potrebnih da se napravi i zapali cigareta:
papir, duhan ili šibice. Svaki pušač ima različiti sastojak, tj. jedan ima samo papir, drugi duhan i treći samo 
šibice. Trgovac ima sve tri komponente u neograničenim količinama. Trgovac nasumice odabire dvije različite komponente,
stavlja ih na stol te signalizira pušačima da su sastojci na stolu. Jedino pušač kojemu nedostaju sastojci stavljeni na
stol, smije uzeti oba sastojka, signalizirati trgovcu te potom saviti cigaretu i popušiti ju. Trgovac tada 
stavlja nova dva sastojka na stol i postupak se ponavlja.

Zadatak
Ispravno sinkronizirati 3 procesa pušača i jednog procesa trgovca koristeći:

- protokol s putujućom značkom (rješavaju studenti čija je zadnja znamenka JMBAG neparna).

Svi procesi ispisuju poruku koju šalju i poruku koju primaju.

Sve što u zadatku nije zadano, riješiti na proizvoljan način.
"""

'''
A B C i T
T ima 3 znacke a, b, c
T -> A -> B -> C -> T
T posalje jednu znacku, kad se vrati priceka malo, ali prima znacke,
kad zavrsi s cekanjem posalje novu znacku od onih koje trenutno ima

'''

start = time()
time_to_run = 10

res_have = {'a': 'papir', 'b': 'duhan', 'c': 'sibice'}
res_miss = {'a': 'duhan i sibice', 'b': 'papir i sibice', 'c': 'papir i duhan'}


def trgovac(mq_prev, mq_next):
    tokens = ['a', 'b', 'c']
    token = random.choice(tokens)
    tokens.remove(token)
    mq_next.send(token, True)
    print("Trgovac sent %s" % res_miss[token])
    last_sent = time()
    while time() - start < time_to_run:
        try:
            msg = mq_prev.receive(timeout=0.001)[0].decode("utf-8")
            print("Trgovac received %s" % res_miss[msg])
            tokens.append(msg)
        except posix_ipc.BusyError as e:
            pass
        if len(tokens) > 0:
            if time() - last_sent > 1:
                token = random.choice(tokens)
                tokens.remove(token)
                mq_next.send(token, True)
                print("Trgovac sent %s" % res_miss[token])
                last_sent = time()
    print("Trgovac done")


def pusac(id, mq_prev, mq_next):
    my_id = id
    while time() - start < time_to_run:
        try:
            msg = mq_prev.receive(timeout=0.001)[0].decode("utf-8")
            print("Pusac %s received %s" % (res_have[id], res_miss[msg]))
            if msg == my_id:
                print("Pusac %s takes resource %s" % (res_have[id], res_miss[msg]))
                sleep(0.1)
                print("Pusac %s sends %s" % (res_have[id], res_miss[msg]))
                mq_next.send(msg, True)
            else:
                print("Pusac %s sends %s" % (res_have[id], res_miss[msg]))
                mq_next.send(msg, True)

        except posix_ipc.BusyError as e:
            pass
    print("Pusac %s done" % (res_have[id],))


def main():
    mq_TA = posix_ipc.MessageQueue("/1", posix_ipc.O_CREAT)
    mq_AB = posix_ipc.MessageQueue("/2", posix_ipc.O_CREAT)
    mq_BC = posix_ipc.MessageQueue("/3", posix_ipc.O_CREAT)
    mq_CT = posix_ipc.MessageQueue("/4", posix_ipc.O_CREAT)
    p_t = Process(target=trgovac, args=(mq_CT, mq_TA))
    p_a = Process(target=pusac, args=('a', mq_TA, mq_AB))
    p_b = Process(target=pusac, args=('b', mq_AB, mq_BC))
    p_c = Process(target=pusac, args=('c', mq_BC, mq_CT))

    p_t.start()
    p_a.start()
    p_b.start()
    p_c.start()

    p_t.join()
    p_a.join()
    p_b.join()
    p_c.join()

    mq_CT.close()
    mq_TA.close()
    mq_AB.close()
    mq_BC.close()


def test():
    try:
        mq = posix_ipc.MessageQueue("/my_q", flags=posix_ipc.O_CREAT)

        mq.send("message to C program", True)

        msg = mq.receive(timeout=0.001)

        print(msg)

        # mq.close()
    except Exception as e:
        print("ERROR: message queue creation failed", e)


if __name__ == '__main__':
    main()
