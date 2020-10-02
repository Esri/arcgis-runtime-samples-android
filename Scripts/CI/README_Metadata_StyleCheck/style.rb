all  # opt-in all rules by default
rule 'MD003', :style => :atx  # Header style written as non-closing pound marks. e.g. ## Section title
rule 'MD004', :style => :asterisk  # Unordered list style as asterisk, rather than hyphen or plus sign
rule 'MD029', :style => :ordered  # Ordered list item prefix is incremental, rather than all ones
exclude_rule 'MD013'  # not limiting line length
exclude_rule 'MD007'  # not limiting unordered list indentation, tab, 2 or 4 spaces are all fine