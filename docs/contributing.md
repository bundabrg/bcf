# Contributing

Here are some ways that you can help contribute to this project.

## New ideas or Bug Reports

Need something? Found a bug? Or just have a brilliant idea? Head to the [Issues](https://github.com/Bundabrg/bcf/issues) and create new one.

## Contributing Code

If you know Java then take a look at open issues and create a pull request.

Do the following to build the code:

```shell
git clone https://github.com/Bundabrg/bcf
cd bcf
mvn clean package
```

## Contributing Documentation

If you can help improve the documentation it would be highly appreciated. Have a look under the `docs` folder for the existing documentation.

The documentation is built using `mkdocs`. You can set up a hot-build dev environment that will auto-refresh changes as they are made.

### Requirements

* python3
* pip3
* npm (only if changing themes)

Install a virtual environment under a subfolder `venv`

```
python3 -m venv venv
```

Enter environment

```
source venv/bin/activate
```

Install dependencies by running:

```
pip3 install -r requirements.txt
```

### Dev Environment

To start a http document server on `http://127.0.0.1:8000` execute:

```
mkdocs serve
```
