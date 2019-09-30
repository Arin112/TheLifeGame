#define _USE_MATH_DEFINES
#include <GL/freeglut.h>
#include <chrono>
#include <cmath>
#include <cstdlib>
#include <experimental/coroutine>
#include <iostream>
#include <range/v3/all.hpp>
#include <range/v3/experimental/utility/generator.hpp>
#include <thread>
#include <type_traits>
#include <vector>

using namespace ranges;
using std::vector;
using namespace std::chrono_literals;
auto &&tr = view::transform;
auto &&acc = accumulate;
auto &&io = view::iota;

vector<vector<int>> pole;
bool go = true, slow = false;
int mod(int a, int b) { return (a % b + b) % b; }

ranges::experimental::generator<double> linspace(double s, double e, int cnt) {
	double dx = (e - s) / cnt++, n = 0;
	while (cnt--)
		co_yield s + dx *n++;
}

void circle(float x, float y, float r) {
	glBegin(GL_TRIANGLE_FAN);
	glVertex2f(x, y);
	for (auto d : linspace(0, 2 * M_PI, 15))
		glVertex2f(x + (r * cos(d)), y + (r * sin(d)));

	glEnd();
}
auto resizePole = [](int x, int y) ->vector<vector<int>> {
	return io(0, x) | tr([&](int) {
			   return vector<int>(io(0, y) |
								  tr([&](int) { return rand() % 2; }));
		   });
};
int main(int argc, char **argv) {
	glutInit(&argc, argv);
	glutInitDisplayMode(GLUT_RGBA | GLUT_DOUBLE);
	glutInitWindowPosition(50, 50);
	glutInitWindowSize(600, 600);
	glutCreateWindow("The life Game");
	pole = resizePole(100, 100);
	glutKeyboardFunc([](unsigned char ch, int x, int y) {
		auto W(glutGet(GLUT_WINDOW_WIDTH)), H(glutGet(GLUT_WINDOW_HEIGHT));
		int X(pole.size()), Y(pole[0].size());
		if (ch == 'a')
			pole[int(x / W * X)][int((H - y) / H * Y)] = 1;
		if (ch == 'd')
			pole[int(x / W * X)][int((H - y) / H * Y)] = 0;
		if (ch == 'q')
			exit(0);
		if (ch == ' ')
			go = !go;
		if (ch == 'r')
			pole = resizePole(X, Y);
		if (ch == 'c')
			pole = io(0, x) | tr([&](int) { return vector<int>(y); });
		if (ch == '+')
			pole = resizePole(X + 1, Y + 1);
		if (ch == '-')
			pole = resizePole(std::max(0, X - 1), std::max(0, Y - 1));
		if (ch == 't')
			slow = !slow;
	});
	while (1) {
		auto sm = [](int x, int y) {
			return acc(io(x - 1, x + 2) | tr([&](int i) {
						   return acc(io(y - 1, y + 2) | tr([&](int j) {
										  int X(pole.size()), Y(pole[0].size());
										  return pole[mod(i, X)][mod(j, Y)];
									  }),
									  0);
					   }),
					   0);
		};
		auto isAlive = [&](int x, int y) {
			return (pole[x][y] &&
					any_of(io(3, 5), [&](int v) { return v == sm(x, y); })) ||
				   (!pole[x][y] && sm(x, y) == 3);
		};
		glClear(GL_COLOR_BUFFER_BIT);
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();

		int X(pole.size()), Y(pole[0].size());
		for_each(io(0, X), [&](int i) {
			for_each(io(0, Y), [&](int j) {
				if (pole[i][j])
					circle(2. / X * i - 1 + 1. / X, 2. / Y * j - 1 + 1. / Y,
						   1. / X);
			});
		});
		if (go)
			pole = io(0, X) | tr([&](int i) {
					   return vector<int>(
						   io(0, Y) | tr([&](int j) { return isAlive(i, j); }));
				   });
		glutSwapBuffers();
		glutMainLoopEvent();
		if (slow)
			std::this_thread::sleep_for(100ms);
	}
	return 0;
}
