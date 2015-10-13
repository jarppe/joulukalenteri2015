# Joulukalenteri 2015

Simple ClojureScript application to represent my daughters Joulukalenteri for christmas 2015.

Running app is at http://millan-joulukalenteri.fi

# Development

Requires [boot](https://github.com/boot-clj/boot) to build.

```bash
boot dev
```

# Build

```bash
boot package
```

# Deployment

Requires [Ansible](http://www.ansible.com) and [Vagrant](https://www.vagrantup.com).

```bash
cd ansible
vagrant up
ansible-playbook site.yml -i inventory/vagrant -u vagrant
```

# License

## Artistic content

All images Copyright © 2015 Milla Länsiö

The "Joulukalenteri 2015" images by Milla Länsiö are licensed under a [Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License](http://creativecommons.org/licenses/by-nc-sa/4.0/).

## Code

All other parts Copyright © 2015 Jarppe Länsiö

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
